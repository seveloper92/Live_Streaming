package com.example.dbsdn.mychat;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.kakao.usermgmt.LoginButton;

import java.util.ArrayList;
import java.util.List;

public class Login_ExpandableListAdapter extends RecyclerView.Adapter {
    public static final int HEADER = 0;
    public static final int CHILD = 1;

    public List<Item> data;
    //어댑터에 data를 담아주는것이다.
    int set = 1;
    public Login_ExpandableListAdapter(List<Item> data) {
        this.data = data;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {

        View view = null;
        Context context = parent.getContext ();
        float dp = context.getResources ().getDisplayMetrics ().density;
        int subItemPaddingLeft = (int) (18 * dp);
        int subItemPaddingTopAndBottom = (int) (5 * dp);

        switch (type) {
            case HEADER:
                LayoutInflater inflater = (LayoutInflater) parent.getContext ().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate (R.layout.list_header, parent, false);
                ListHeaderViewHolder header = new ListHeaderViewHolder (view);
                return header;
            case CHILD:
                for (int i = set; i <= data.size()-1; i++) {
                    set++;
                    Log.e("밖에 for문", "i값은 =="+i+"");
                    Log.e("데이터사이즈", data.size() + "");
                    Log.e("CHILD", i + "번째 데이터는  " + data.get(i).text + "");

                    if (data.get(i).text.equals("구글로그인")) {
                        Log.e("구글로그인","들어왔다");
                        //이부분손봐보자
                        SignInButton google_login_button = new SignInButton(context);
                        google_login_button.setPadding(10, 0, 10, 0);
                        google_login_button.setLayoutParams(
                                new ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT)
                        );
                        Log.e("구글로그인","나갔다");
                        return new RecyclerView.ViewHolder(google_login_button) {
                        };
                    } else if (data.get(i).text.equals("카카오로그인")) {
                        Log.e("카카오로그인","들어왔다");
                        LoginButton btn_kakao_login = new LoginButton(context);
                        btn_kakao_login.setPadding(10, 0, 10, 0);
                        btn_kakao_login.setLayoutParams(
                                new ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT)
                        );
                        Log.e("카카오로그인","나갔다");
                        return new RecyclerView.ViewHolder(btn_kakao_login) {
                        };
                    }

                }//end for()_1


        }
        return null;
    }


    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Item item = data.get (position);
        switch (item.type) {
            case HEADER:
                final ListHeaderViewHolder itemController = (ListHeaderViewHolder) holder;
                itemController.refferalItem = item;
                itemController.header_title.setText (item.text);
                if (item.invisibleChildren == null) {


                    Handler handler = new Handler ();
                    final Runnable r = new Runnable () {
                        public void run() {
                            item.invisibleChildren = new ArrayList<Item> ();
                            int count = 0;
                            int pos = data.indexOf (itemController.refferalItem);
                            while (data.size () > pos + 1 && data.get (pos + 1).type == CHILD) {
                                item.invisibleChildren.add (data.remove (pos + 1));
                                count++;
                            }
                            notifyItemRangeRemoved (pos + 1, count);

                        }
                    };
                    handler.post (r);
                    itemController.btn_expand_toggle.setImageResource (R.drawable.arrow_up);
                } else {
                    itemController.btn_expand_toggle.setImageResource (R.drawable.arrow_down);
                }

                itemController.header_title.setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {
                        if (item.invisibleChildren == null) {
                            item.invisibleChildren = new ArrayList<Item> ();
                            int count = 0;
                            int pos = data.indexOf (itemController.refferalItem);
                            while (data.size () > pos + 1 && data.get (pos + 1).type == CHILD) {
                                item.invisibleChildren.add (data.remove (pos + 1));
                                count++;
                            }
                            notifyItemRangeRemoved (pos + 1, count);
                            itemController.btn_expand_toggle.setImageResource (R.drawable.arrow_down);
                        } else {
                            int pos = data.indexOf (itemController.refferalItem);
                            int index = pos + 1;
                            for (Item i : item.invisibleChildren) {
                                data.add (index, i);
                                index++;
                            }
                            notifyItemRangeInserted (pos + 1, index - pos - 1);
                            itemController.btn_expand_toggle.setImageResource (R.drawable.arrow_up);
                            item.invisibleChildren = null;
                        }
                    }
                });

                itemController.btn_expand_toggle.setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {
                        if (item.invisibleChildren == null) {
                            item.invisibleChildren = new ArrayList<Item> ();
                            int count = 0;
                            int pos = data.indexOf (itemController.refferalItem);
                            while (data.size () > pos + 1 && data.get (pos + 1).type == CHILD) {
                                item.invisibleChildren.add (data.remove (pos + 1));
                                count++;
                            }
                            notifyItemRangeRemoved (pos + 1, count);
                            itemController.btn_expand_toggle.setImageResource (R.drawable.arrow_down);
                        } else {
                            int pos = data.indexOf (itemController.refferalItem);
                            int index = pos + 1;
                            for (Item i : item.invisibleChildren) {
                                data.add (index, i);
                                index++;
                            }
                            notifyItemRangeInserted (pos + 1, index - pos - 1);
                            itemController.btn_expand_toggle.setImageResource (R.drawable.arrow_up);
                            item.invisibleChildren = null;
                        }
                    }
                });
                break;
            case CHILD:

                break;
        }

    }

    @Override
    public int getItemViewType(int position) {
        return data.get (position).type;
    }


    @Override
    public int getItemCount() {
        return data.size ();
    }

    private static class ListHeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView header_title;
        public ImageView btn_expand_toggle;
        public Item refferalItem;

        public ListHeaderViewHolder(View itemView) {
            super (itemView);
            header_title = (TextView) itemView.findViewById (R.id.header_title);
            btn_expand_toggle = (ImageView) itemView.findViewById (R.id.btn_expand_toggle);
        }
    }

    public static class Item {
        public int type;
        public String text;
        public List<Item> invisibleChildren;



        public Item(int type, String text) {
            this.type = type;
            this.text = text;

        }

    }
}
