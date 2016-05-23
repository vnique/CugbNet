package cn.wydewy.cugbnet;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.dow.android.DOW;
import cn.dow.android.listener.DLoadListener;
import cn.dow.android.listener.DataListener;


public class DMOffWallActivity extends Activity implements OnClickListener {
	private String TAG = DMOffWallActivity.class.toString();
	private TextView showPointTV;
	private EditText pointEt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dm_off_wall);
		findViewById(R.id.open_offerwall).setOnClickListener(this);
		findViewById(R.id.checkpoints).setOnClickListener(this);
		findViewById(R.id.consumePoints).setOnClickListener(this);

		showPointTV = (TextView) findViewById(R.id.showPoints);
		pointEt = (EditText) findViewById(R.id.consumePoint);

		// 初始化积分墙
		// userid 为用户唯一标识,没有用户账户系统的可以不填
		DOW.getInstance(this).init("userid", new DLoadListener() {

			@Override
			public void onSuccess() {
				Log.v(TAG, "积分墙初始化完成");
			}

			@Override
			public void onStart() {
				Log.v(TAG, "积分墙初始化开始");
			}

			@Override
			public void onLoading() {
				Log.v(TAG, "积分墙初始化中...");
			}

			@Override
			public void onFail() {
				Log.v(TAG, "积分墙初始化失败");
			}
		});

		checkPoints();

	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.open_offerwall:
			openOfferWall();
			break;
		case R.id.consumePoints:
			consumePoint();
			break;
		case R.id.checkpoints:
			checkPoints();
			break;

		default:
			break;
		}
	}

	/**
	 * 打开积分墙
	 */
	private void openOfferWall() {
		DOW.getInstance(this).show(this);
	}

	/**
	 * 查询积分
	 */
	private void checkPoints() {
		DOW.getInstance(this).checkPoints(new DataListener() {
			@Override
			public void onResponse(Object... point) {
				// 用户总的积分数
				double totalPoint = (Double) point[1];
				// 用户的已消费积分数
				double consumPoint = (Double) point[0];
				// 用户的剩余积分数
				double lastPoint = totalPoint - consumPoint;
				showPointTV.setText("总积分:" + totalPoint + "\n已消费积分:"
						+ consumPoint + "\n剩余积分:" + lastPoint);
			}

			@Override
			public void onError(String errorInfo) {

			}
		});
	}

	/**
	 * 消费积分
	 */
	private void consumePoint() {
		int consumePoint;
		String pointStr = pointEt.getText().toString();
		if (TextUtils.isEmpty(pointStr)) {
			return;
		} else {
			try {
				consumePoint = Integer.parseInt(pointStr);
			} catch (Exception e) {
				Toast.makeText(this, "请输入整数类型的数字", Toast.LENGTH_SHORT).show();
				return;
			}
		}

		DOW.getInstance(this).consumePoints(consumePoint, new DataListener() {

			@Override
			public void onResponse(Object... point) {
				// 积分消费的状态
				int status = (Integer) point[0];
				// 用户总的积分数
				double totalPoint = (Double) point[2];
				// 用户的已消费积分数
				double consumPoint = (Double) point[1];
				// 用户的剩余积分数
				double lastPoint = totalPoint - consumPoint;
				switch (status) {
				case 1: // 消费成功
					showPointTV.setText("总积分:" + totalPoint + "\n已消费积分:"
							+ consumPoint + "\n剩余积分：" + lastPoint);
					break;
				case 2: 
					// 积分不足，消费失败
					// 积分不变
					break;
				case 3: 
					// 订单重复
					// 积分不变
					break;
				}
			}

			@Override
			public void onError(String errorInfo) {
			}
		});
	}

}
