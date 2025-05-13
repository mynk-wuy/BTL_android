#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

const char* ssid = "Hello world";
const char* password = "987654321#";

const String serverUrl = "http://192.168.1.11/btl/get_status.php";
const String updateUrl = "http://192.168.1.11/btl/update_status.php";  // Endpoint để gửi trạng thái mới

const int ledPin1 = 2;    // Nhận trạng thái đèn từ server (den1)
const int buttonPin = 27; // Nút điều khiển đèn 2
const int ledPin2 = 32;   // Điều khiển đèn 2 (và gửi trạng thái lên server)

int buttonState = 0;
int lastButtonState = 0;
bool led2State = false;

unsigned long lastCheck = 0;
const unsigned long checkInterval = 5000;

void setup() {
  Serial.begin(115200);
  pinMode(ledPin1, OUTPUT);
  digitalWrite(ledPin1, LOW);

  pinMode(ledPin2, OUTPUT);
  digitalWrite(ledPin2, led2State);

  pinMode(buttonPin, INPUT);

  // Kết nối WiFi
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi Connected!");
}

void loop() {
  // 1. Đọc nút nhấn và cập nhật led2
  buttonState = digitalRead(buttonPin);
  if (buttonState == HIGH && lastButtonState == LOW) {
    led2State = !led2State;
    digitalWrite(ledPin2, led2State);
    Serial.println(led2State ? "LED2 ON" : "LED2 OFF");
    sendLED2StatusToServer(led2State);
    delay(200); // chống dội nút
  }
  lastButtonState = buttonState;

  // 2. Đọc trạng thái từ server mỗi 5 giây
  if (millis() - lastCheck > checkInterval) {
    lastCheck = millis();
    getStatusFromServer();
  }
}

// Gửi trạng thái LED2 lên server (den2)
void sendLED2StatusToServer(bool state) {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(updateUrl);
    http.addHeader("Content-Type", "application/x-www-form-urlencoded");

    String postData = "device=den2&status=" + String(state ? 1 : 0) + "&fullname=NutVatLy";
    int httpCode = http.POST(postData);

    if (httpCode == HTTP_CODE_OK) {
      Serial.println("Cập nhật trạng thái den2 thành công.");
    } else {
      Serial.println("Lỗi gửi trạng thái: " + String(httpCode));
    }

    http.end();
  }
}

// Nhận trạng thái từ server và điều khiển đèn
void getStatusFromServer() {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(serverUrl);
    int httpCode = http.GET();

    if (httpCode == HTTP_CODE_OK) {
      String payload = http.getString();
      Serial.println("Received: " + payload);

      DynamicJsonDocument doc(2048);
      DeserializationError error = deserializeJson(doc, payload);

      if (!error) {
        for (JsonObject obj : doc.as<JsonArray>()) {
          String device = obj["device"];
          int status = obj["status"];

          if (device == "den1") {
            digitalWrite(ledPin1, status == 1 ? HIGH : LOW);
          }

          if (device == "den2") {
            led2State = (status == 1);
            digitalWrite(ledPin2, led2State);  // cập nhật trạng thái mới cho LED2
          }
        }
      } else {
        Serial.println("Lỗi parse JSON");
      }
    } else {
      Serial.println("Lỗi HTTP: " + String(httpCode));
    }

    http.end();
  }
}