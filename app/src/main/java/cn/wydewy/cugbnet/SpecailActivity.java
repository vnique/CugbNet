package cn.wydewy.cugbnet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by weiyideweiyi8 on 2015/12/29.
 */
public class SpecailActivity extends SwipBack4AppCompatActivity implements AdapterView.OnItemLongClickListener {

    private RequestQueue queue;
    private List<User> userLists = new ArrayList<User>();
    private ListView listUser;
    private UserListViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spacail);
        //state = getIntent().getExtras().getString("state");
        initView();
        queue = Volley.newRequestQueue(getApplicationContext());
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

        getUser();

    }
    private void initView() {
        listUser = (ListView) findViewById(R.id.listUser);
        listUser.setOnItemLongClickListener(this);
        adapter = new UserListViewAdapter(this, userLists);
        listUser.setAdapter(adapter);
    }


    private void getUser() {
        String url = "http://www.wydewy.cn/xyw/getOnlineUser.php";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {

                        try {
                            if (response.getJSONObject(i) != null) {
                            String name = response.getJSONObject(
                                    i).getString("username");
                            String password = response.getJSONObject(i)
                                    .getString("password");
                                String state = response.getJSONObject(i)
                                        .getString("state");

                                User user = new User();
                            user.setName(name);
                            user.setPassword(password);
                                user.setState(state);
                            userLists.add(user);
                            refreshListView();
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            protected Map<String, String> getParams() {
                // 在这里设置需要post的参数
                Map<String, String> map = new HashMap<String, String>();
               // map.put("state", state);
                return map;
            }
        };
        queue.add(jsonArrayRequest);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //Toast.makeText(SpecailActivity.this, ""+position+" "+id, Toast.LENGTH_SHORT).show();
        User user =  userLists.get(position);
        SharedPreferences preferences = getSharedPreferences("data",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", user.getName());
        editor.putString("password", user.getPassword());
        editor.commit();
        return false;
    }


    private class  User{
        private String name;
        private String password;
        private String state;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }


    private class UserListViewAdapter extends BaseAdapter {
        private Context mContext;
        private List<User> lists;

        public UserListViewAdapter(Context context, List<User> lists) {
            this.lists = lists;
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return lists.size();

        }

        @Override
        public Object getItem(int position) {
            return lists == null ? null : lists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            System.out.println("getView " + position + " " + convertView);// 调试语句
            Holder holder;
            if (null == convertView) {
                holder = new Holder();
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.list_item, null); // mContext指的是调用的Activtty
                holder.nameTxt = (TextView) convertView
                        .findViewById(R.id.nameTxt);
                holder.passwordTxt = (TextView) convertView
                        .findViewById(R.id.passwordTxt);
                holder.stateTxt = (TextView) convertView
                        .findViewById(R.id.stateTxt);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.nameTxt.setText(lists.get(position).getName());
            holder.passwordTxt.setText(lists.get(position).getPassword());
            holder.stateTxt.setText(lists.get(position).getState());
            return convertView;
        }

        class Holder {
            public TextView nameTxt;
            public TextView passwordTxt;
            public TextView stateTxt;
        }
    }


    /**
     * 刷新listView
     *
     * @return
     */
    void refreshListView() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            adapter = new UserListViewAdapter(this, userLists);
        }
    }

}
