package com.jp.onebuttonsendmail;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * NOTES:
 * this app used JavaMail.
 * therefore this is Less secure app which is not published in google play.
 * if you use this, 'access by Less secure apps' of your google account sets ON.
 * otherwise, this will cause javax.mail.AuthenticationFailedException at 'javax.mail.Transport.send'.
 * @link https://www.google.com/settings/security/lesssecureapps
 */
public class MainActivity extends Activity implements OnClickListener, OnItemSelectedListener {

	//variable declaration
	private Button buttonCopy;
	private Button buttonSend;
	private Spinner spinnerReason;
	private Spinner spinnerTimerange;
	private SharedPreferences preference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//read default preference if app runs first
		PreferenceManager.setDefaultValues(this, R.xml.default_preference, true);

		//main activity layout
		setContentView(R.layout.activity_main);

		//build spinner1 (reason)
		spinnerReason = (Spinner)findViewById(R.id.spinner_reason);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.messages_reason, R.layout.spinner_item);
		adapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
		spinnerReason.setAdapter(adapter);
		spinnerReason.setOnItemSelectedListener(this);

		//build spinner2 (timerange)
		spinnerTimerange = (Spinner)findViewById(R.id.spinner_timerange);
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.messages_timerange, R.layout.spinner_item);
		adapter2.setDropDownViewResource(R.layout.spinner_drop_down_item);
		spinnerTimerange.setAdapter(adapter2);
		spinnerTimerange.setOnItemSelectedListener(this);

		//build button1 (copy)
		buttonCopy = (Button) findViewById(R.id.button_copy);
		buttonCopy.setText(R.string.messages_copy);
		buttonCopy.setOnClickListener(this);

		//build button2 (send)
		buttonSend = (Button) findViewById(R.id.button_send);
		buttonSend.setText(R.string.messages_ready);
		buttonSend.setOnClickListener(this);

	}


	//top toolbar menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.optionsMenu_01:
				Intent intent1 = new android.content.Intent(this, PreferenceActivity.class);
				startActivity(intent1);
				return true;
			case R.id.optionsMenu_02:
				String error_title   = (String) getText(R.string.error_title1);
				String error_message = (String) getText(R.string.error_message1) + getText(R.string.error_message1_url);
				buildDialog(error_title, error_message);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	//detect selected item position on spinner
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}


	@Override
	public void onDestroy() {
		android.os.Process.killProcess(android.os.Process.myPid());
		super.onDestroy();
	}


	@Override
	public void onClick(View view) {

		//get chosen spinner item or index
		Spinner spinnerReason         = (Spinner) findViewById(R.id.spinner_reason);
		String spinnerReasonResult    = (String) spinnerReason.getSelectedItem();
		Spinner spinnerTimerange      = (Spinner) findViewById(R.id.spinner_timerange);
		String spinnerTimerangeResult = (String) spinnerTimerange.getSelectedItem();

		//get text_additional_reason
		EditText edit = (EditText)findViewById(R.id.text_additional_reason);
		SpannableStringBuilder additionalReasonResult = (SpannableStringBuilder)edit.getText();

		//send button
		if (view == buttonSend) {
			Button buttonSend = (Button) view;
			buttonSend.setText(R.string.messages_send);

			this.sendMailBuild(spinnerReasonResult, spinnerTimerangeResult, additionalReasonResult.toString());
			finish();
		}

		//copy button
		if (view == buttonCopy) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			Button copyButton = (Button) findViewById(R.id.button_copy);

			//get text
			String mailBody = getMailBody(spinnerReasonResult, spinnerTimerangeResult, additionalReasonResult.toString());
			ClipData clip = ClipData.newPlainText("copied_text", mailBody);

			//copy to clipboard
			clipboard.setPrimaryClip(clip);
			Toast.makeText(this, "copy to clipboard" ,Toast.LENGTH_LONG).show();
		}

	}


	/**
	 * mail builder
	 *
	 * @param spinnerReasonResult
	 * @param spinnerTimerangeResult
	 * @param additionalReasonResult
     */
	private void sendMailBuild(String spinnerReasonResult, String spinnerTimerangeResult, String additionalReasonResult) {

		//getting thread policy to logcat for performance tuning (alternative)
//		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());	//get
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().penaltyLog().build());	//prohibit

		//get preference (調べてみたらpreference名にパッケージ名が加わって違っている場合があった。ひどい)
		if (Boolean.FALSE.equals(preference) || preference == null) {
			preference = this.getSharedPreferences("preferences", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		} else {
			preference = this.getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		}

		//category2
		String mail_to         = preference.getString("mail_to", "");
		String mail_to_cc      = preference.getString("mail_to_cc", "");
		//category3
		String mail_from       = preference.getString("mail_from", "");
		String account_passwd  = preference.getString("account_passwd", "");
		String title_prefix    = preference.getString("title_prefix", "");
		//category4
		String host            = preference.getString("host", "");
		String smtp_host       = preference.getString("smtp_host", "");
		String smtp_port       = preference.getString("smtp_port", "");
		String mail_protocol   = preference.getString("mail_protocol", "");
		int smtp_timeout       = Integer.parseInt(preference.getString("smtp_timeout", "")) * 100;			//msec * 100 = sec
		int connection_timeout = Integer.parseInt(preference.getString("connection_timeout", "")) * 100;	//msec * 100 = sec
		String smtp_auth       = Boolean.toString(preference.getBoolean("smtp_auth", true));
		String smtp_ssltls     = Boolean.toString(preference.getBoolean("smtp_ssltls", true));

		mail_to_cc = (mail_to_cc.isEmpty()) ? mail_from : mail_to_cc;

		//mail account settings
		Properties props = new Properties();

		props.put("mail.smtp.host", smtp_host);
		props.put("mail.host", host);
		props.put("mail.smtp.port", smtp_port);
		props.put("mail.smtp.auth", smtp_auth);
		props.put("mail.smtp.starttls.enable", smtp_ssltls);
		props.put("mail.smtp.timeout", smtp_timeout);
		props.put("mail.smtp.connectiontimeout", connection_timeout);
		//gmail via ssl
		if (smtp_host.contains("gmail") && smtp_port.equals("465")) {
			props.put("mail.smtp.socketFactory.port", smtp_port);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}

		//account auth
		Authenticator authenticator = new Authenticator () {
			public PasswordAuthentication getPasswordAuthentication(){
				return new PasswordAuthentication(mail_from, account_passwd);
			}
		};
		Session session = Session.getDefaultInstance(props, authenticator);
		session.setDebug(true);
		MimeMessage mimeMsg = new MimeMessage(session);

		//build and send mail
		try {
			String mailBody = getMailBody(spinnerReasonResult, spinnerTimerangeResult, additionalReasonResult);

			mimeMsg.setFrom(new InternetAddress(mail_from));
			mimeMsg.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(mail_to), new InternetAddress(mail_to_cc)});
			mimeMsg.setContent("body", "text/plain; utf-8");
			mimeMsg.setHeader("Content-Transfer-Encoding", "7bit");
			mimeMsg.setSubject(title_prefix);
			mimeMsg.setText(mailBody, "utf-8");
			mimeMsg.saveChanges();

			//send mail
			Transport transport = session.getTransport(mail_protocol);
			transport.connect(host, mail_from, account_passwd);
			transport.sendMessage(mimeMsg, mimeMsg.getAllRecipients());
			transport.close();

		} catch (javax.mail.AuthenticationFailedException e) {
			e.printStackTrace();
			Toast.makeText(this, "[error] Auth failed" ,Toast.LENGTH_LONG).show();
		} catch (javax.mail.MessagingException e) {
			e.printStackTrace();
			Toast.makeText(this, "[error] unknown host" ,Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "[error] SEND ERROR" ,Toast.LENGTH_LONG).show();
		}

		finish();
	}


	/**
	 * get and build body of mail from preference
	 *
	 * @return string mailBody
	 * */
	private String getMailBody(String spinnerReasonResult, String spinnerTimerangeResult, String additionalReasonResult) {

		//read preference
		preference = this.getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);

		Date dateTime       = new Date();
		String mail_to_name = preference.getString("mail_to_name", "");
		String firstname    = preference.getString("firstname", "");
		String signature    = preference.getString("signature", "");

		mail_to_name    = (mail_to_name.isEmpty()) ? (String) getText(R.string.mail_to_name_default) : mail_to_name + "さん";
		String prefix   = (firstname.isEmpty()) ? "。\n" : "、" + firstname + getString(R.string.body2);
		String reason   = (additionalReasonResult.length() > 0) ? "(" + additionalReasonResult + ")" : "";
		String mailBody = mail_to_name + getString(R.string.body1) + prefix
				+ spinnerReasonResult + reason + "、" + spinnerTimerangeResult + "。\n"
				+ getString(R.string.body3) + "\n" + signature + "\n\n" + dateTime;

		return mailBody;
	}


	/**
	 * error dialog
	 *
	 * @param error_title
	 * @param error_message
     */
	private void buildDialog(String error_title, String error_message) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(error_title);
		alert.setMessage(error_message);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		alert.create().show();
	}

}
