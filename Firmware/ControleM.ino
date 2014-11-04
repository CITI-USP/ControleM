#define ANALOG_MODE 0
#define DIGITAL_MODE 1
#define START_CMD_CHAR '*'
#define DATA_SIZE 6
#define D_FRONT 9
#define D_BACK 8
#define D_RIGHT 7
#define D_LEFT 6
#define D_BUTTON 5
#define AXIS_MAX 4095
#define AXIS_MIN 0
#define AXIS_MID 2047
#define WAIT_TIME 200

boolean debug = true;
byte mode = ANALOG_MODE;
char get_char = ' ';
byte data[DATA_SIZE];
int x;
int y;
int flag_register;
byte return_code;
int timer;


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

  if(debug) Serial.begin(9600);
  Serial1.begin(9600);
  Serial1.flush();
  timer = millis();
}

void loop() {
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
    if(debug)  for(int i=0;i<DATA_SIZE;i++)  Serial.println(String("data")+i+String("=")+data[i]);
    for(int i=0;i<DATA_SIZE;i++)
    
    timer = millis();
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
      
      a tensão real ficou entre 6,8v e 5,2v
      checksum e sequencial
      analogWrite(DAC0, map(x,0,4095,1638,2457));//out (1638 a 2457) metade de 4095 = 2047 sendo que 1638(-10% de vcc) e 2457(+10% vcc) padrão para joystick de cadeira de rodas
      analogWrite(DAC1, map(y,0,4095,1638,2457));//out (1638 a 2457) metade de 4095 = 2047 sendo que 1638(-10% de vcc) e 2457(+10% vcc) padrão para joystick de cadeira de rodas
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
  }
}

