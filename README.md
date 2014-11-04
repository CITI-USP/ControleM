ControleM
=========

É um emulador de joystick analógico de cadeira de rodas motorizada controlado por Smartphone.

O joystick analógico de cadeira de rodas possuem um comportamento padrão, mais informações em  [Interfaces de entrada para Cadeira de rodas motorizada](http://marchanjo.blogspot.com.br/2013/07/interface-de-entrada-para-cadeira-de.html).

A essência para emular um joystick é produzir um sinal de referência de 6v e dois (x e y) sinais analógicos reais com 6v e variação de 10% de alimentação (1,2v para alimentação de 12v) para mais ou para menos, ou seja de 4,8v até 7,2v. Abaixo a figura de um joystick analógico padrão:

[![Joystick analógico JC200](http://3.bp.blogspot.com/-hqsU5Gdmz_E/Ud2zLGnaRcI/AAAAAAAABj8/vSfEUgOKIYg/s1600/JC200.jpg)
*Joystick JC200 (Part Number: JC200BS1K1Y de 12V ) fonte: Farnell*](http://www.farnellnewark.com.br/chavetipojoystick12vdc,product,01M8005,4614452.aspx)

Como funciona
-------------------
Uma [Arduino Due](http://arduino.cc/en/Main/ArduinoBoardDue) com um circuito adicional é alimentado pela cadeira de rodas motorizada (12v 100mA), como se fosse um joystick analógio padrão. A Arduino Due possui duas saídas DAC ((digital to analog) de 3,3v (Arduino Due  trabalha com 3,3v) com amplificadores operacionais esta saídas são elevadas para 12v, sendo que são programadas para ficarem no meio da escala (6v) e variar 1,2v para mais ou para menos.

Um módulo Bluetooth serial provê a comunicação remota sem fio, através de um protocolo simples e robusto são recebidos os dados necessário para controlar as saídas DAC.

A figura abaixo apresenta o circutio necessário:
![Arduino Due e Shield](https://dl.dropboxusercontent.com/u/42132965/controlem/Arduino%20Due%20e%20Shield.jpg)*Arduino Due e Shield*



Repository Contents
-------------------
* **/Firmware** - Código Arduino
* **/Hardware** - Eagle design files (.brd, .sch)
* **/App** - Código da aplicação android

Product Versions
----------------
* [PRT-12084](https://www.sparkfun.com/products/12084)- Version 1.0

Version History
---------------
* [v10](https://github.com/sparkfun/SunnyBuddy/tree/HW_V1.0) -GitHub files for v1.0

License Information
-------------------
O hardware e o software estão disponiveis em  [Creative Commons ShareAlike 4.0 International](https://creativecommons.org/licenses/by-sa/4.0/).

The hardware and the software are released under [Creative Commons ShareAlike 4.0 International](https://creativecommons.org/licenses/by-sa/4.0/).


Distribuido como está, nenhuma garantia é dada.

Distributed as-is; no warranty is given.
