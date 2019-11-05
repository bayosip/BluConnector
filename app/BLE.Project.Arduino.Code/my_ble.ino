#include <SPI.h>
#if not defined (_VARIANT_ARDUINO_DUE_X_) && not defined (_VARIANT_ARDUINO_ZERO_)
#include <SoftwareSerial.h>
#endif

#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_UART.h"
#include "BluefruitConfig.h"
#include <string.h>

#define LED_R 						3
#define LED_G 						5
#define LED_B 						6
#define BTTN_ONOFF  				2
#define	NUM_COLORS					7
#define BUFSIZE     				128
#define ADC 						255
#define RSSI	 					"AT+BLEGETRSSI"
#define MINIMUM_FIRMWARE_VERSION    "0.6.6"
#define FACTORYRESET_ENABLE      	true
#define NEXT						"next"
#define PREVIOUS					"back"
#define ONOFF						"on/off"
#define BRIGHT						"bright"
#define DIM							"dark"

float colors[NUM_COLORS][3] = {{1.0,1.0,1.0},{1.0,0.0,0.0},{1.0,1.0,0.0},
							   {0.0,1.0,0.0},{0.0,1.0,1.0},
							   {0.0,0.0,1.0},{1.0,0.0,1.0}};

int currentColor =		0;
volatile int ledState = 0;
int fadeAmount = 		5;    // how many points to fade the LED by
int brightness = 		ADC; // how bright the LED is
	// Check for user input

	
/* The service information */
int32_t ledServiceId;
int32_t ledONOFFCharId;
int32_t ledStatusCharId;
uint32_t i;

void fadeOff(int color);
void lightUpLed(int i, int br);
void turnOnOffLed (int state);
void checkButtonOnOFF (void);
void fadeOn (int color);
void configureBLE(void);
void error(const __FlashStringHelper*err);
char* talkBLE(void);
void updateLedStatus(void);
void changeToNextColor (void);
void changeToPreviousColor (void);
void dimLights(void);
void brightenLights(void);

SoftwareSerial bluefruitSS = SoftwareSerial(BLUEFRUIT_SWUART_TXD_PIN, BLUEFRUIT_SWUART_RXD_PIN);

Adafruit_BluefruitLE_UART ble(bluefruitSS, BLUEFRUIT_UART_MODE_PIN,
                      BLUEFRUIT_UART_CTS_PIN, BLUEFRUIT_UART_RTS_PIN);

// A small Flash print helper
void error(const __FlashStringHelper*err) {
  Serial.println(err);
  while (1);
}

void setup(){
		configBLE();
		pinMode(LED_R, OUTPUT);
		pinMode(LED_G, OUTPUT);
		pinMode(LED_B, OUTPUT);
}

void configBLE (void){
	
  boolean success;
  Serial.begin(115200);
  Serial.println(F("Adafruit Bluefruit UART DATA mode Example"));
  Serial.println(F("-------------------------------------"));

  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));

  if ( !ble.begin(VERBOSE_MODE) )
  {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }
  Serial.println( F("OK!") );

  if ( FACTORYRESET_ENABLE )
  {
    /* Perform a factory reset to make sure everything is in a known state */
    Serial.println(F("Performing a factory reset: "));
    if ( ! ble.factoryReset() ){
      error(F("Couldn't factory reset!"));
    }
  }

  /* Disable command echo from Bluefruit */
  ble.echo(false);
  
  /* disable verbose mode */
  ble.verbose(false);
  
  Serial.println(F("Setting device name to Osi_p BLE-LED`"));
  if (! ble.sendCommandCheckOK(F( "AT+GAPDEVNAME=Osi_p BLE-LED Controller" )) ) {
    error(F("Could not set device name!"));
  }
  
  Serial.println(F("Clearing previous Custom Service"));
  success = ble.sendCommandCheckOK( F("AT+GATTCLEAR"));
  if (! success) {
    error(F("Could not Clear service"));
  }
  
  Serial.println(F("Adding the LED status service definition(UUID = fe-33-e6-80-e2-0b-11-e5-aa-34-00-02-a5-d5-c5-1b): "));
  success = ble.sendCommandCheckOK( F("AT+GATTADDSERVICE=UUID128=fe-33-e6-80-e2-0b-11-e5-aa-34-00-02-a5-d5-c5-1b"));
  if (! success) {
    error(F("Could not add LED Status service"));
  }
  
  /* Add the LED status characteristic */
  /* Chars ID for Measurement should be 1 */
  Serial.println(F("Adding the LED Control characteristic (UUID = 0x0002): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x0002, PROPERTIES=0x08, MIN_LEN=1, VALUE=0"), &ledONOFFCharId);
    if (! success) {
    error(F("Could not add LED Status characteristic"));
  }

  /* Add the LED status characteristic */
  /* Chars ID for Measurement should be 1 */
  Serial.println(F("Adding the LED Status characteristic (UUID = 0x0003): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x0003, PROPERTIES=0x10, MIN_LEN=1, MAX_LEN=20, VALUE=Off"), &ledStatusCharId);
    if (! success) {
    error(F("Could not add LED Status characteristic"));
  }

  Serial.println(F("Please use app to connect arduino."));
  Serial.println(F("Awaiting connection..."));
  Serial.println();

  /* Wait for connection */
  while (! ble.isConnected()) {
      delay(500);
  }

  // Set module to DATA mode
  Serial.println( F("Device connected: Switching to DATA mode!") );
  ble.setMode(BLUEFRUIT_MODE_DATA);
  

  Serial.println(F("******************************"));
}

void readBleRssi(void){
	int n;
	// check response stastus
	// if (ble.available()) {
   		ble.println(RSSI);
   		ble.waitForOK();
	//}
//	return i;
}

char* talkBLE(void){
	char n, inputs[BUFSIZE+1];

  // Echo received data
  if (ble.available()) {
  	n = ble.readline(inputs, BUFSIZE+1);
  	ble.waitForOK();
  	inputs[n] = 0;
  
  	// display to user
  	Serial.print("Receiving: ");
  	Serial.println(inputs);
  	if (inputs !=NULL)
  	return inputs;
	}
}

void loop(){
	
	 //readBleRssi();
	char* instruct = talkBLE();
	
	while(instruct != NULL){
	if(strcmp(instruct,ONOFF)==0){
		ledState = !ledState;
  			Serial.println(F("Turn on or off LED"));
			turnOnOffLed(ledState);
			updateLedStatus();
			Serial.println(F("Waiting for device instruction...."));
			break;
	}
	
	else if (strcmp(instruct, "on")==0){
		ledState = 1;
		Serial.println(F("Turn on LED"));
			turnOnOffLed(ledState);
			updateLedStatus();
			Serial.println(F("Waiting for device instruction...."));
			break;
	}
	
	else if (strcmp(instruct, "off")==0){
		ledState = 0;
		Serial.println(F("Turn off LED"));
			turnOnOffLed(ledState);
			updateLedStatus();
			Serial.println(F("Waiting for device instruction...."));
			break;
	}
		
	else if ( strcmp(instruct,NEXT)==0){
		 	 Serial.println(F("Change to next color"));
			 changeToNextColor ();
			 Serial.println(F("Waiting for device instruction...."));
			 break;
  	}
  	
  	else if ( strcmp(instruct,PREVIOUS)==0){
		 	 Serial.println(F("Change to previous color"));
			 changeToPreviousColor ();
			 Serial.println(F("Waiting for device instruction...."));
			 break;
  	}
  	
  	else if ( strcmp(instruct,DIM)==0){
  			Serial.println(F("Dim lights"));
  			dimLights();
  			Serial.println(F("Waiting for device instruction...."));
  			break;
  	}
  	
  	else if ( strcmp(instruct,BRIGHT)==0){
  		Serial.println(F("Brighten lights"));
  		brightenLights();
  		Serial.println(F("Waiting for device instruction...."));
  		break;
  		}
  	else{
  		Serial.println(F("Instuction Error!"));
  		Serial.println(F("Waiting for device instruction...."));
  		break;
  	}
	}
}

void updateLedStatus(void){
	if (ledState ==1){
		ble.print( F("AT+GATTCHAR=") );
  		ble.println( ledStatusCharId );
  		ble.print( F(", LED is: "));
  		ble.println(F("On"));
	}
	else{
		ble.print( F("AT+GATTCHAR=") );
  		ble.println( ledStatusCharId );
  		ble.print( F(", LED is: "));
  		ble.println(F("Off"));
	}
}

void changeToNextColor (void){
	if (ledState ==1){
		fadeOff(currentColor);
  		if (++currentColor == NUM_COLORS) currentColor = 0;
  		fadeOn(currentColor);}
  	else {
  			Serial.println(F("LED not on!!!"));
  		}
}

void changeToPreviousColor (void){
	if (ledState ==1){
		fadeOff(currentColor);
  		if (--currentColor <0 ) currentColor = 6;
  		fadeOn(currentColor);}
  			else {
  			Serial.println(F("LED not on!!!"));
  		}
}

void dimLights(void){
	if (ledState ==1){
	while (brightness >1){
  			lightUpLed(currentColor, brightness);
  			brightness = brightness >>1;
  		// break of at lowest intensity
		if (brightness == 1)break;
		delay(25);
			}
	}
  			else {
  			Serial.println(F("LED not on!!!"));
  		}
}

void brightenLights(void){
	if (ledState ==1){
		while(brightness < ADC){
  			lightUpLed(currentColor, brightness);
  			brightness = brightness <<1;

  		// break off at max intensity:
			if (brightness == ADC) break;
			delay(25);
		}
	}
  			else {
  			Serial.println(F("LED not on!!!"));
  		}
}

void fadeOn (int color){
	
	while (brightness < ADC){
  			lightUpLed(color, brightness);
  			brightness = brightness + fadeAmount;

  		// break out once at full brightness:
		if (brightness == ADC) {
    		break;
  		}
  		// wait for 20 milliseconds to see the dimming effect
  			delay(25);
	}
}

void fadeOff(int color){
	while (brightness >0){
  			lightUpLed(color, brightness);
  			brightness = brightness - fadeAmount;

  		// reverse the direction of the fading at the ends of the fade: 
		if (brightness == 0) {
    		break;
  		}   
  		// wait for 20 milliseconds to see the dimming effect
		delay(25);
	}
}

void lightUpLed(int i, int br){
	
	analogWrite(LED_R, (colors[i][0])*br);
	analogWrite(LED_G, (colors[i][1])*br);
	analogWrite(LED_B, (colors[i][2])*br);
}

void turnOnOffLed (int state){
	
	analogWrite(LED_R, (colors[currentColor][0])*(brightness * state));
	analogWrite(LED_G, (colors[currentColor][1])*(brightness * state));
	analogWrite(LED_B, (colors[currentColor][2])*(brightness * state));
}