package com.example.dbsdn.mychat;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dbsdn.mychat.Retrofit.ResponseGet;
import com.example.dbsdn.mychat.Retrofit.RetroCallback;
import com.example.dbsdn.mychat.Retrofit.RetroClient;
import com.example.dbsdn.mychat.databinding.ActivitySignBinding;

import java.util.HashMap;
/************************************/
/**   회원가입을 진행하는 액티비티   **/

/************************************/
public class SignActivity extends AppCompatActivity {

    //레트로핏
    RetroClient retroClient;

    TextInputLayout Layout_Enter_Id;
    TextInputEditText EditText_Enter_Id;

    TextInputLayout EditText_Email;
    TextInputEditText EMAIL_edit;

    TextInputLayout Layout_Enter_Password;
    TextInputEditText PWedit;

    TextInputLayout Layout_Password_Check;
    TextInputEditText PW_CK_edit;

    Button Button_Id_Check;
    Button Button_Sign_Up;
    ImageView ImageView_Password_Check;
    String ID,EMAIL,PASSWORD; //값담을 변수
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        //레트로핏
        retroClient = RetroClient.getInstance(this).createBaseApi();

        /**   아이디입력 뷰   **/
        Layout_Enter_Id =(TextInputLayout)findViewById(R.id.Layout_Enter_Id);
        EditText_Enter_Id = (TextInputEditText)findViewById(R.id.IDedit);

        /**   이메일입력 뷰   **/
        EditText_Email =(TextInputLayout)findViewById(R.id.EditText_Email);
        EMAIL_edit = (TextInputEditText)findViewById(R.id.EMAIL_edit);

        /**   비밀번호입력 뷰   **/
        Layout_Enter_Password =(TextInputLayout)findViewById(R.id.Layout_Enter_Password);
        PWedit = (TextInputEditText)findViewById(R.id.PWedit);
        /**   비밀번호확인입력 뷰   **/
        Layout_Password_Check =(TextInputLayout)findViewById(R.id.Layout_Password_Check);
        PW_CK_edit = (TextInputEditText)findViewById(R.id.PW_CK_edit);
        /**   아이디중복체크 버튼 뷰   **/
        Button_Id_Check = (Button)findViewById(R.id.Button_Id_Check);
        /**   아이디중복체크 버튼 초기에 비활성화   **/
        Button_Id_Check.setEnabled(false);
        /**   회원가입 완료 버튼 뷰   **/
        Button_Sign_Up = (Button)findViewById(R.id.Button_Sign_Up);
        /**   회원가입 완료 버튼 초기에 비활성화   **/
        Button_Sign_Up.setEnabled(false);

        /**   비밀번호를 두 번 일치하게 입력했는지 보여주기 위한 이미지 뷰   **/
        ImageView_Password_Check = (ImageView)findViewById(R.id.ImageView_Password_Check);


        /**   비밀번호 확인 입력 시 앞서 입력한 비밀번호와 비교하기 위한 이벤트처리   **/
        PW_CK_edit.addTextChangedListener(new TextWatcher() {
            /**   입력되기 전에   **/
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            /**   입력되는 EditText에 변화가 있을 때   **/
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(PWedit.getText().toString().equals(PW_CK_edit.getText().toString())){
                    ImageView_Password_Check.setImageResource(R.drawable.o);
                    Button_Sign_Up.setEnabled (true);

                }
                else{
                    ImageView_Password_Check.setImageResource(R.drawable.x);
                    Toast.makeText(getApplicationContext(),"비밀번호가 일치하지 않습니다",Toast.LENGTH_LONG).show();
                    Button_Sign_Up.setEnabled (false);
                }
            }

            /**   EditText의 입력이 끝났을 때   **/
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0){
                    ImageView_Password_Check.setImageResource(0);
                }
            }
        });



        /**   아이디입력 시 maxlength(지정해놓은 최대 길이)가 넘어가면 발생되는 이벤트처리   **/
        EditText_Enter_Id.addTextChangedListener(new TextWatcher() {
            /**   입력되기 전에   **/
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            /**   입력되는 EditText에 변화가 있을 때   **/
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Button_Id_Check.setEnabled(true);
            }

            /**   EditText의 입력이 끝났을 때   **/
            @Override
            public void afterTextChanged(Editable s) {

                /**   maxlength(지정해놓은 최대 길이)가 넘어가면 아래와 같은 메세지를 출력   **/
                if (s.length() > Layout_Enter_Id.getCounterMaxLength()){
                    Layout_Enter_Id.setError("아이디는 " + Layout_Enter_Id.getCounterMaxLength() +"문자까지 가능합니다.");
                    Button_Id_Check.setEnabled(false);
                    Button_Sign_Up.setEnabled (false);
                    EditText_Enter_Id.setBackgroundColor(R.drawable.rounded3);
                }
                //길이가 0이면
                else if (s.length ()==0){
                    Button_Id_Check.setEnabled(false);
                    Button_Sign_Up.setEnabled (false);
                }
                else {
                    Button_Sign_Up.setEnabled (true);
                }


            }
        });


        //아이디 체크
        Button_Id_Check.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                //데이터 삽입
                ID = EditText_Enter_Id.getText().toString();
                Log.e("체크버튼데이터정보", ID+EMAIL+PASSWORD);


                //데이터 전송.
                HashMap<String, Object> parameters = new HashMap<>();
                parameters.put("id", ID);

                Log.e("아이디체크 해쉬맵결과", String.valueOf(parameters));
                retroClient.postSecond(parameters, new RetroCallback() {
                    @Override
                    public void onError(Throwable t) {
                        Log.e("체크onError도착", t.toString());
                        //activitySignBinding.textView.setText("Error");

                    }
                    @Override
                    public void onSuccess(int code, Object receivedData) {
                        Log.e("체크onSuccess", "ㅇㅇ");
                        //요청하고 받은 값을 ResponseGet에 넣어 분리 한 후.
                        ResponseGet data = (ResponseGet) receivedData;
                        //분리한 값중 pw값에 따라서 결과를 다르게줌.
                        if(data.pw.equals ("1")){
                            Toast.makeText(getApplicationContext(),data.email,Toast.LENGTH_LONG).show();
                        }
                        else if (data.pw.equals ("2")){
                            Toast.makeText(getApplicationContext(),data.email,Toast.LENGTH_LONG).show();
                            Button_Sign_Up.setEnabled(false);
                        }

                        Toast.makeText(getApplicationContext(),data.email,Toast.LENGTH_LONG).show();
                        //여기에 php에서 받은 데이터가 1이면 회원가입 완성. 아니면 버튼비활성화.

                        Log.e ("받아오는값",""+data.id+" "+data.pw+""+data.email);
                    }

                    @Override
                    public void onFailure(int code) {
                        Log.e("체크onFailure", "중복 확인 안댐");


                    }
                });

            }
        });




        Button_Sign_Up.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {  //데이터 삽입
                ID = EditText_Enter_Id.getText().toString();
                EMAIL = EMAIL_edit.getText().toString();
                PASSWORD = PWedit.getText().toString();

                //회원가입 예외처리.
                if (ID.equals (null)&&PASSWORD.equals (null)&&PASSWORD.equals (null)){
                    Toast.makeText(getApplicationContext(),"회원정보가 비었습니다.",Toast.LENGTH_LONG).show();
                    Button_Sign_Up.setEnabled(false);
                }
                else if (ID.equals ("")&&PASSWORD.equals ("")&&EMAIL.equals ("")){
                    Toast.makeText(getApplicationContext(),"회원정보가 비었습니다.",Toast.LENGTH_LONG).show();
                    Button_Sign_Up.setEnabled(false);
                }
                else if (PASSWORD.equals ("")){
                    Toast.makeText(getApplicationContext(),"회원정보가 비었습니다.",Toast.LENGTH_LONG).show();
                    Button_Sign_Up.setEnabled(false);
                }
                else if (ID.equals ("")){
                    Toast.makeText(getApplicationContext(),"회원정보가 비었습니다.",Toast.LENGTH_LONG).show();
                    Button_Sign_Up.setEnabled(false);
                }
                else if (EMAIL.equals ("")){
                    Toast.makeText(getApplicationContext(),"회원정보가 비었습니다.",Toast.LENGTH_LONG).show();
                    Button_Sign_Up.setEnabled(false);
                }
                else {
                    //데이터 전송.
                    HashMap<String, Object> parameters = new HashMap<>();
                    parameters.put("id", ID);
                    parameters.put("email", EMAIL);
                    parameters.put("pw", PASSWORD);
                    Log.e("보낸데이터", String.valueOf(parameters));
                    retroClient.postFirst(parameters, new RetroCallback() {
                        @Override
                        public void onError(Throwable t) {
                            Log.e("onError", t.toString());
                            //activitySignBinding.textView.setText("Error");

                        }

                        @Override
                        public void onSuccess(int code, Object receivedData) {
                            Log.e("onSuccess", "ㅇㅇ");
                            ResponseGet data = (ResponseGet) receivedData;
                            finish();
                            // activitySignBinding.textView.setText (data.id+"   " + data.email+ "   " + data.pw);
                            Log.e("onFailure", "ㅇㅇ" + data.id + "   " + data.email + "   " + data.pw);

                        }

                        @Override
                        public void onFailure(int code) {
                            Log.e("onFailure", "회원 데이터 전송안댐");

//                codeResultTextView.setText(code);
                            //activitySignBinding.textView.setText("Failure");

                        }
                    });


                    ID = EditText_Enter_Id.getText().toString();
                    EMAIL = EMAIL_edit.getText().toString();
                    PASSWORD = PWedit.getText().toString();

                    //에딧텍스트 초기화
                    EditText_Enter_Id.setText("");
                    EMAIL_edit.setText("");
                    PW_CK_edit.setText("");
                    PWedit.setText("");

                    Log.e("데이터 보내짐", "");
                    Toast.makeText(getApplicationContext(), "회원가입완료", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}
