/*============블루투스 시리얼 선언 =============*/
#include <SoftwareSerial.h>
SoftwareSerial mySerial(2, 3); // RX, TX
/*===========================================*/


/*===============온습도 선언===============*/
#include "DHT.h"
#define DHTPIN 4        // SDA 핀의 설정
#define DHTTYPE DHT22   // DHT22 (AM2302) 센서종류 설정
DHT dht(DHTPIN, DHTTYPE);
/*=======================================*/

/*===========미세먼지 선언 START===========*/
unsigned long pulse = 0;
float ugm3 = 0;
float ugm5 = 0;
/*============미세먼지 선언 END============*/

void setup() {
  Serial.begin(9600);
  mySerial.begin(9600);
  while (!Serial) {
    //; // wait for serial port to connect. Needed for native USB port only
  }
  
  dht.begin();
  /*====미세먼지  디지털 PIN====*/
  pinMode(8, INPUT); //8번
  /*==========================*/

}

void loop() {
  if (mySerial.available()) { //블루투스에서 넘어온 데이터가 있다면
    Serial.write(mySerial.read()); //시리얼모니터에 데이터를 출력
  }
  if (Serial.available()) {    //시리얼모니터에 입력된 데이터가 있다면
    mySerial.write(Serial.read());  //블루투스를 통해 입력된 데이터 전달
  }
  
  /*==========온/습도 센서 동작부================*/
  float h = dht.readHumidity();
  float t = dht.readTemperature();
  //온도, 습도 표시 시리얼 모니터 출력
  Serial.print("Humidity: ");
  Serial.print(h);
  Serial.print('\t');
  Serial.print(" Temperature: ");
  Serial.print(t);
  Serial.print('\t');
  /*===========================================*/

  /*==========미세먼지센서 동작부================*/
  pulse = pulseIn(8, LOW, 20000);
  //Serial.println(pulse);    // 기본 데이터

  ugm3 = pulse2ugm3(pulse); //초미세먼지 함수 불러오기
  ugm5 = pulse2ugm5(pulse);
  //미세먼지 함수 불러오기

  
  //Serial.print('\t');// [초 미세먼지]
  Serial.print("mug2.5");
  Serial.print('\t');
  Serial.print(ugm3);
  Serial.print('\t');

  //Serial.print(ugm5);    // [미세먼지] (ugm5,4)소수4째 저장
  Serial.print("mug10");
  Serial.print('\t');
  Serial.println(ugm5);
  /*===========================================*/

  //mySerial.print(ugm3); //초미세먼지
  //mySerial.print(',');
  mySerial.print(ugm3); //미세먼지
  mySerial.print(',');
  mySerial.print(h); //습도
  mySerial.print(',');
  mySerial.print(t); //온도
  mySerial.println();
  
  delay(1000);
}

/*=======================미세먼지센서 동작함수====================*/
float pulse2ugm3(unsigned long pulse) { //초 미세먼지
  float value = (pulse - 1190) / 9.8;  //먼지는 1610 / 10.2
  if (value > 300) {   //300넘으면 초기화
    value = 0;
  }
  return value;
}


float pulse2ugm5(unsigned long pulse) { //미세먼지
  float value = (pulse - 1400) / 10.0;  //환산
  if (value > 300) {   //300넘으면 초기화
    value = 0;
  }
  return value;
}
/*===========================================================*/
