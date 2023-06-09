package kr.co.company.pfsi_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// 챗봇 음성채팅 액티비티 클래스 (2023-05-13 인범)
// 05-17 챗봇 API 연동
// 05-20 채팅 UI 구현
public class ChatVoiceActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private String inputResult, chatResult;
    private RecyclerView recyclerView;
    private EditText inputEditText;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatItem> chatItems;

    private LinearLayoutManager layoutManager;

    private ImageButton voiceButton;

    private String userMessage, chatbotMessage;

    public static Context mContext;

    Intent sttIntent;
    SpeechRecognizer mRecognizer;
    TextToSpeech tts;
    final int PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_voice);
        mContext = this;

        recyclerView = findViewById(R.id.recyclerView);
        inputEditText = findViewById(R.id.inputEditText);
        sendButton = findViewById(R.id.sendButton);
        voiceButton = findViewById(R.id.voiceButton);

        layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        chatItems = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatItems);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        // 오디오, 카메라 권한설정
        if (Build.VERSION.SDK_INT >= 23) {
            // 퍼미션 체크
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO
            }, PERMISSION);
        }

        // STT, TTS 로드
        speechInit();

        // 음성 입력 버튼
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 문자입력 비활성화
                sendButton.setClickable(false);
                inputEditText.setClickable(false);
                inputEditText.setFocusable(false);

                voiceButton.setImageResource(R.drawable.mike_on);

                // 음성입력 시작
                speechStart();
            }
        });

        // 전송 버튼
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userMessage = inputEditText.getText().toString().trim();
                if (!userMessage.isEmpty()) {
                    // 사용자의 메시지를 ChatItem에 추가
                    ChatItem userChatItem = new ChatItem();
                    userChatItem.setMessage(userMessage);
                    userChatItem.setSenderName("사용자");
                    userChatItem.setProfileImageResId(R.drawable.user);
                    userChatItem.setTimestamp(new Date());
                    chatItems.add(userChatItem);

                    // 입력값 챗봇 전달
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                chatbotMessage = chatbotRequest(userMessage);
                                chatbotMessage = chatbotMessage.substring(1, -2);
                                Log.d("chatbotMessage", chatbotMessage);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            (ChatVoiceActivity.this).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 채팅 응답 출력
                                    // 챗봇의 응답을 ChatItem에 추가
                                    ChatItem botChatItem = new ChatItem();
                                    botChatItem.setMessage(chatbotMessage);
                                    botChatItem.setSenderName("프로비");
                                    botChatItem.setProfileImageResId(R.drawable.chatbot);
                                    botChatItem.setTimestamp(new Date());
                                    chatItems.add(botChatItem);

                                    chatAdapter.notifyDataSetChanged();

                                    // RecyclerView에 아이템이 추가될 때마다 포커싱 맞추기
                                    int newItemPosition = chatItems.size() - 1;
                                    layoutManager.scrollToPosition(newItemPosition);

                                    inputEditText.setText("");

                                    // 채팅 응답 음성 출력
                                    funcVoiceOut(chatbotMessage);
                                }
                            });
                        }
                    }).start();

                }
            }
        });
    }


    private void speechInit() {
        // stt 객체 생성, 초기화
        sttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        // tts 객체 생성, 초기화
        tts = new TextToSpeech(ChatVoiceActivity.this, this);
    }


    public void speechStart() {
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext); // 음성인식 객체
        mRecognizer.setRecognitionListener(listener); // 음성인식 리스너 등록
        mRecognizer.startListening(sttIntent);
    }


    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }
            String guideStr = "에러가 발생하였습니다.";
            Toast.makeText(getApplicationContext(), guideStr + message, Toast.LENGTH_SHORT).show();
            funcVoiceOut(guideStr);

            // 텍스트 입력 활성화
            sendButton.setClickable(true);
            inputEditText.setFocusable(true);
            inputEditText.setClickable(true);

            voiceButton.setImageResource(R.drawable.mike_off);
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            inputResult = "";

            for (int i = 0; i < matches.size(); i++) {
                inputResult += matches.get(i);
            }

            if (!inputResult.isEmpty()) {
                // 사용자의 메시지를 ChatItem에 추가
                ChatItem userChatItem = new ChatItem();
                userChatItem.setMessage(inputResult);
                userChatItem.setSenderName("사용자");
                userChatItem.setProfileImageResId(R.drawable.user);
                userChatItem.setTimestamp(new Date());
                chatItems.add(userChatItem);

                // 입력값 챗봇 전달
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            chatbotMessage = chatbotRequest(inputResult);
                            chatbotMessage = chatbotMessage.substring(1, -2);
                            Log.d("chatbotMessage", chatbotMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        (ChatVoiceActivity.this).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 채팅 응답 출력
                                // 챗봇의 응답을 ChatItem에 추가
                                ChatItem botChatItem = new ChatItem();
                                botChatItem.setMessage(chatbotMessage);
                                botChatItem.setSenderName("프로비");
                                botChatItem.setProfileImageResId(R.drawable.chatbot);
                                botChatItem.setTimestamp(new Date());
                                chatItems.add(botChatItem);

                                chatAdapter.notifyDataSetChanged();

                                // RecyclerView에 아이템이 추가될 때마다 포커싱 맞추기
//                                int newItemPosition = chatItems.size() - 1;
//                                layoutManager.scrollToPosition(newItemPosition);

                                inputEditText.setText("");

                                // 채팅 응답 음성 출력
                                funcVoiceOut(chatbotMessage);
                            }
                        });
                    }
                }).start();

            }

            // 텍스트 입력 활성화
            sendButton.setClickable(true);
            inputEditText.setFocusable(true);
            inputEditText.setClickable(true);

            voiceButton.setImageResource(R.drawable.mike_off);

        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };

    public void funcVoiceOut(String OutMsg) {
        if (OutMsg.length() < 1) return;
        if (!tts.isSpeaking()) {
            tts.speak(OutMsg, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.KOREAN);
            tts.setPitch(1);
        } else {
            Log.e("TTS", "초기화 실패");
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (mRecognizer != null) {
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer = null;
        }
        super.onDestroy();
    }

    // chatbotAPI 요청
    private String chatbotRequest(String userInput) throws Exception {
        try {
            URL url = new URL("https://chatbotapi-gpt-inofu.run.goorm.site/piuda");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject data = new JSONObject();
            data.put("user_input", userInput);

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(data.toString());
            out.flush();
            out.close();

            String temp = "";
            String content = "";
            InputStream responseBody = conn.getInputStream();
            InputStreamReader responseBodyReader =
                    new InputStreamReader(responseBody, "UTF-8");
            BufferedReader br = new BufferedReader(responseBodyReader);
            while ((temp = br.readLine()) != null) {
                content += temp;
            }
            JSONObject responseJson = new JSONObject(content);
            Log.d("chatGPT 응답", responseJson.toString(2));
            br.close();

            String jsonMessage = responseJson.toString(2);

            // 응답 값만 추출
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonMessage, JsonObject.class);
            String valueMessage = jsonObject.get("piuda").getAsString();

            return valueMessage;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "서버에 문제가 발생하였습니다. 나중에 다시 시도해주세요", Toast.LENGTH_SHORT).show();
        }
        return "";
    }
}