ControleM
=========

É um emulador de joystick analógico de cadeira de rodas motorizada controlado por Smartphone.

|Vídeo de demonstração - protótipo com controle proporcional (Multi direcional) | Vídeo de demonstração -       protótipo com controle discreto (4 direções)|
|---------|---------|
|[![Vídeo de demonstração proprocional ](http://img.youtube.com/vi/2mQoy-6fy2M/0.jpg)](https://www.youtube.com/watch?v=2mQoy-6fy2M)|[![Vídeo de demonstração discreto](http://img.youtube.com/vi/nm2pQ9PgypI/0.jpg)](https://www.youtube.com/watch?v=nm2pQ9PgypI)

O joystick analógico de cadeira de rodas possuem um comportamento padrão, mais informações em  [Interfaces de entrada para Cadeira de rodas motorizada](http://marchanjo.blogspot.com.br/2013/07/interface-de-entrada-para-cadeira-de.html).

A essência para emular um joystick é produzir um sinal de referência de 6v e dois (x e y) sinais analógicos reais com 6v e variação de 10% de alimentação (1,2v para alimentação de 12v) para mais ou para menos, ou seja de 4,8v até 7,2v. Abaixo a figura de um joystick analógico padrão:

![Joystick analógico JC200](http://3.bp.blogspot.com/-hqsU5Gdmz_E/Ud2zLGnaRcI/AAAAAAAABj8/vSfEUgOKIYg/s1600/JC200.jpg)

*Joystick JC200 (Part Number: JC200BS1K1Y de 12V ). Fonte: [Farnell](http://www.farnellnewark.com.br/chavetipojoystick12vdc,product,01M8005,4614452.aspx)*

Como funciona
-------------
Uma [Arduino Due](http://arduino.cc/en/Main/ArduinoBoardDue) com um circuito adicional é alimentado pela cadeira de rodas motorizada (12v 100mA), como se fosse um joystick analógio padrão. A Arduino Due possui duas saídas DAC ((digital to analog) de 3,3v (Arduino Due  trabalha com 3,3v) com amplificadores operacionais esta saídas são elevadas para 12v, sendo que são programadas para ficarem no meio da escala (6v) e variar 1,2v para mais ou para menos.

Um módulo Bluetooth serial provê a comunicação remota sem fio, através de um protocolo simples e robusto são recebidos os dados necessário para controlar as saídas DAC.O módulo Bluettoh teve a velocidade ajutada para 115200 bps.

A figura abaixo apresenta o circuto necessário:

*Esquemático:*
![Esquemático](https://github.com/Marchanjo/ControleM/blob/master/Doc/ControleM.png)

*Arduino Due e Shield:*
![Arduino Due e Shield](https://github.com/Marchanjo/ControleM/blob/master/Doc/ArduinoDueShield.jpg)

*Arduino Due e Shield montada:*
![Arduino Due e Shield montada](https://github.com/Marchanjo/ControleM/blob/master/Doc/ArduinoDueShieldMontadas1.jpg)

Protocolo de comunicação
------------------------

A placa e o smartphone se conectam por meio de Bluetooth.

O smartphone deve enviar pacotes seriais de 8 bytes seguindo o protocolo abaixo:

|Sequência | Descrição                               |
|----------|-----------------------------------------|
|Byte 0    | Início da sequência (*)                 |
|Byte 1    | Coordenada X - byte mais significativo  |
|Byte 2    | Coordenada X - byte menos significativo |
|Byte 3    | Coordenada Y - byte mais significativo  |
|Byte 4    | Coordenada Y - byte menos significativo |
|Byte 5    | Flag register - primeiro byte           |
|Byte 6    | Flag register - segundo byte            |
|Byte 7    | Número de sequência                     |
|Byte 8    | Byte de verificação - Checksum          |

Ao reconhecer o valor do byte 0 como o caracter *, o firmware inicia a sequência de leitura do pacote.

Os bytes 1 e 2 contém a coordenada X do joystick, um valor de 0 a 4093.

Os bytes 3 e 4 contém a coordenada Y do joystick, um valor de 0 a 4093.

Os bytes 5 e 6 são um conjunto de bits para enviar informações genéricas do smartphone para a App. No exemplo atual, apenas o último bit do byte 6 é usado, para informar o modo de operação do firmware.

O byte 7 é um byte que contém o número de sequência do pacote enviado, e é usado para verificar se um mesmo pacote não foi enviado mais de uma vez por problema de conexão entre placa e smartphone.

O byte 8 é um byte de verificação, que contém a soma dos bytes anteriores (exceto o byte 0), e é usado para confirmar que não houve corrupção do pacote enviado.

Mapa do projeto
---------------
* **/Firmware** - Código Arduino
* **/Hardware** - Arquivos de design, formato Eagle
* **/App** - Código da aplicação Android
* **/Doc** - Arquivos adicionais usados no Read Me
* **/Bin** - Arquivos binários

Licença
-------
O hardware e o software estão disponiveis em [Creative Commons Attribution-ShareAlike 4.0 International License.](http://creativecommons.org/licenses/by-sa/4.0/)![by-sa-4.0](https://i.creativecommons.org/l/by-sa/4.0/88x31.png)

The hardware and the software are released under [Creative Commons Attribution-ShareAlike 4.0 International License.](http://creativecommons.org/licenses/by-sa/4.0/)![by-sa-4.0](https://i.creativecommons.org/l/by-sa/4.0/88x31.png)


Distribuido como está, nenhuma garantia é dada.

Distributed as-is; no warranty is given.
