package com.example.dbsdn.mychat.LiveChatAdapter;

//채팅 리사이클러뷰 아이템
public class LiveChat_Item {
    public  String name;
    public String chat;


    //변수명으로 만들어서 가져오는건 힘드니까 메소드로 만들어서 가져온다.
    public String getname() {
        return name;
    }

    public String getchat() {
        return chat;
    }

    public LiveChat_Item(String name, String chat){
        this.name = name;
        this.chat = chat;
    }
}//뷰에 뿌릴 내용 작성완료