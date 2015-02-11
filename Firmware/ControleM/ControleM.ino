#define ANALOG_MODE 0
#define DIGITAL_MODE 1
#define START_CMD_CHAR '*'
#define DATA_SIZE 8
#define D_FRONT 9
#define D_BACK 8
#define D_RIGHT 7
#define D_LEFT 6
#define D_BUTTON 5
#define LED 13
#define AXIS_MAX 4095
#define AXIS_MIN 0
#define WAIT_TIME 200

const int REF = 2; 
int AXIS_MID=2047;//variável que receberá a leitura analógica da referência
int reference;


boolean debug = false;
byte mode = ANALOG_MODE;
char get_char = ' ';
byte data[DATA_SIZE];//data[0] - mais sig. X, data[1] - menos sig. X, data[2] - mais sig. Y, data[3] - menos sig. Y, data[4] - Flag Register, data[5] - Flag Register, data[6] - Sequencial do pacote, data[7] - Checksum
int x;
int y;
int flag_register;
byte return_code;
int timer;
byte checksum = 0;
byte last_sequence = 255;//como o celular começa a sequencia com 0, last_sequence é inicializado com 255 para evitar um erro no primeiro pacote



void setup() {
  pinMode(DAC0, OUTPUT);
  pinMode(DAC1, OUTPUT);
  analogWriteResolution(12); //0-4095 limite do DAC da Arduino Due (12 bits)
  
  // Saída analógica é iniciada no meio da escala (ponto morto)
  
  analogWrite(DAC0, AXIS_MID);
  analogWrite(DAC1, AXIS_MID);
  
  // Inicialização dos pinos correspondentes à saida digital
  pinMode(D_FRONT, OUTPUT); digitalWrite(D_FRONT, LOW);
  pinMode(D_BACK, OUTPUT); digitalWrite(D_BACK, LOW);
  pinMode(D_RIGHT, OUTPUT); digitalWrite(D_RIGHT, LOW);
  pinMode(D_LEFT, OUTPUT); digitalWrite(D_LEFT, LOW);
  pinMode(D_BUTTON, OUTPUT); digitalWrite(D_BUTTON, LOW);
  pinMode(LED, OUTPUT); digitalWrite(LED, LOW);

  if(debug) Serial.begin(115200);
  Serial1.begin(115200);
  Serial1.flush();
  timer = millis();
}

void loop() {
  reference=analogRead(REF);
  AXIS_MID=map(reference,0,1023,0,4095);//atualiza a posição central baseado no sinal lido no Analog 2
  if(debug) Serial.println(String("Ref=")+AXIS_MID);
   
  
  // Caso não tenha sido recebido um pacote válido durante WAIT_TIME milisegundos,
  // tanto as saídas do modo digital e analógico do joystick são devolvidas à
  // posição inicial (ponto morto)
  if((millis() - timer) > WAIT_TIME){
    analogWrite(DAC0, AXIS_MID);//força posição central
    analogWrite(DAC1, AXIS_MID);//força posição central 
    digitalWrite(D_FRONT, LOW);
    digitalWrite(D_BACK, LOW);
    digitalWrite(D_RIGHT, LOW);
    digitalWrite(D_LEFT, LOW);
    digitalWrite(D_BUTTON, LOW);
    digitalWrite(LED, LOW);
    if(debug) Serial.println("******************* Ponto Morto *******************");
  }
    
  
  Serial1.flush();
  if (Serial1.available() < 1)  return;
  
  
  get_char = Serial1.read();
  if(debug) Serial.println(String("cmd=")+get_char);
    
  if (get_char != START_CMD_CHAR) return;
  
  return_code = Serial1.readBytes(data, DATA_SIZE);
  if(debug) Serial.println(String("return=")+return_code);
   
  if(return_code==DATA_SIZE) {
    if(debug)  {for(int i=0;i<DATA_SIZE;i++)  Serial.println(String("data")+i+String("=")+data[i]);}
    
    if(data[DATA_SIZE-2]!=last_sequence) {if(debug) Serial.println("Sequence OK!");}
    else{
      if(debug) Serial.println("Sequence Error ! &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
      return;
    }
    last_sequence=data[DATA_SIZE-2];
    
    //checksum
    checksum = 0;
    for (int i=0; i<DATA_SIZE-1; i++)  checksum+=data[i];
       
    if(debug) Serial.println(String("checksum enviado=")+data[DATA_SIZE-1]+ String("checksum calculado=")+checksum);
    if(data[DATA_SIZE-1]==checksum) {if(debug) Serial.println("Checksum OK!");}
    else{
      if(debug) Serial.println("Checksum - Packet Fail! *************************************");
      return;
    }
        
        
    timer = millis();
    digitalWrite(LED, HIGH);
    
    x = data[0] << 8;
    x += data[1];
    y = data[2] << 8;
    y += data[3];
    flag_register = data[4] << 8;
    flag_register += data[5];
       
    
    mode = flag_register & 0x01;
    if(debug) Serial.println(String("X: ") + x  + String(" Y: ") + y + String(" MODE: ") + mode);
    
    
    
    if(mode == ANALOG_MODE) {
      if(x > AXIS_MAX) x = AXIS_MAX;
      if(y > AXIS_MAX) y = AXIS_MAX;
                 
      //Para para joystick de cadeira de rodas o sinal tem que ser 6v (metade de 12v) para ficar parado, 
      //Para a variação positiva 6v + 10% do Vcc (+1,2v) ou 7,2v - na
      //Para a variação netativa 6v - 10% do Vcc (-1,2v) ou 4,8v 
      //Logo a variação é: min=4,8v centro=6v max=7,2v 
      //Nos pinos do DAC: min=1,32v centro=1,65v max=1,98v 
      //Como chegar nos valores do analogWrite de 0 a 4095:
      //Medir o DAC com 0 e com 4095, no meu caso de 0,52v e 2,73v, subtrai as medidas que deu 2,21v (o vcc da placa deu 3,29v)
      //Faz a regra de tres (4095 esta para 2,21v como tensão X está para ? )
      //Para a tensão de 1,32v é necessário descontar o 0,52v, logo a regra de três deve ser 0,80v que deu 1482,353  (arredondado para 1482) 
      //Para a tensão de 1,98v é necessário descontar o 0,52v, logo a regra de três deve ser 1,46v que deu 2705,294 (arredondado para 2705) 

      if(x == 2047) {
        x = AXIS_MID;//AXIS_MID é o valor lido no pino analógico 2 que apresenta valor usado para Ref que irá gerar os 6v de referência, é mais preciso que 2047 enviado do celular (que indica centro do canvas)
        analogWrite(DAC0, AXIS_MID); 
        if(debug) Serial.println("******************* Ponto Morto X *******************");}
      else
        analogWrite(DAC0, map(x,0,4095,1482,2705)); 
        
      if(y == 2047) {
        y = AXIS_MID;//AXIS_MID é o valor lido no pino analógico 2 que apresenta valor usado para Ref que irá gerar os 6v de referência, é mais preciso que 2047 enviado do celular (que indica centro do canvas)
        analogWrite(DAC1, AXIS_MID);
        if(debug) Serial.println("******************* Ponto Morto Y *******************");}
       else
         analogWrite(DAC1, map(y,0,4095,1482,2705));
      
      
    }
    else if(mode == DIGITAL_MODE) {
      if(x == AXIS_MAX) {
        digitalWrite(D_FRONT, HIGH);
        digitalWrite(D_BACK, LOW);
      }
      else if (x == AXIS_MIN){
        digitalWrite(D_FRONT, LOW);
        digitalWrite(D_BACK, HIGH);
      }
      else if (x == AXIS_MID){
        digitalWrite(D_FRONT, LOW);
        digitalWrite(D_BACK, LOW);
      }
      
      if(y == AXIS_MAX) {
        digitalWrite(D_RIGHT, HIGH);
        digitalWrite(D_LEFT, LOW);
      }
      else if(y == AXIS_MIN) {
        digitalWrite(D_RIGHT, LOW);
        digitalWrite(D_LEFT, HIGH);
      }
      else if (y == AXIS_MID){
        digitalWrite(D_RIGHT, LOW);
        digitalWrite(D_LEFT, LOW);
      }
        
      // flag_register tem 16 bits, e cada um pode controlar uma porta digital. Por exemplo:
      int button_value = flag_register >> 8 & 0x01;
      if(button_value) button_value = HIGH; else button_value = LOW;
      digitalWrite(D_BUTTON, button_value);
    }
    digitalWrite(LED, LOW);
  }
}


