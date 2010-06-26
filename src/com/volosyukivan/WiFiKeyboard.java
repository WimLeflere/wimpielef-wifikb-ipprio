package com.volosyukivan;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class WiFiKeyboard extends Activity {
    int port = 7777;
    LinearLayout layout;
    
    private View createView() {
      ArrayList<String> addrs = new ArrayList<String>();
      try {
        Enumeration<NetworkInterface> ifaces =
          NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
          NetworkInterface iface = ifaces.nextElement();
          if ("lo".equals(iface.getName())) continue;
          Enumeration<InetAddress> addresses = iface.getInetAddresses();
          while (addresses.hasMoreElements()) {
            InetAddress addr = addresses.nextElement();
            addrs.add(addr.getHostAddress());
          }
        }
      } catch (SocketException e) {
        Debug.d("failed to get network interfaces");
      }

      ScrollView parent = new ScrollView(this);
      int fill = LinearLayout.LayoutParams.FILL_PARENT;
      int wrap = LinearLayout.LayoutParams.WRAP_CONTENT;
      LinearLayout.LayoutParams fillAll =
        new LinearLayout.LayoutParams(fill, fill);
      parent.setLayoutParams(fillAll);
      layout = new LinearLayout(this);
      layout.setOrientation(LinearLayout.VERTICAL);
      layout.setLayoutParams(new LinearLayout.LayoutParams(fill, wrap));
      parent.addView(layout);
      text(R.string.desc_setup_wifi, 20);
      text(R.string.desc_goto_settings);
      text(R.string.desc_enable_kbd);
      text(R.string.desc_toch_input_field);
      text(R.string.desc_change_input_method);
      text("", 15);
      if (addrs.size() == 0) {
        text("Enable wifi or GPRS/3G", 20);
      } else if (addrs.size() == 1) {
        text(getString(R.string.desc_connect_to_one, addrs.get(0), port), 20);
      } else {
        text(R.string.desc_connect_to_any);
        for (String addr : addrs) {
          text("http://" + addr + ":" + port, 20);
        }
      }
      text(R.string.desc_warn);
      text("", 15);
      text(R.string.desc_alt_usb_cable, 20);
      text(R.string.desc_adb_from_sdk);
      text(getString(R.string.desc_cmdline, port, port), 15);
      text(getString(R.string.desc_connect_local, port), 15);
      return parent;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (this.bindService(new Intent(this, HttpService.class),
            new ServiceConnection() {
          //@Override
          public void onServiceConnected(ComponentName name, IBinder service) {
            Debug.d("WiFiInputMethod connected to HttpService.");
            try {
              int newPort = RemoteKeyboard.Stub.asInterface(service).getPort();
              if (newPort != port) {
                port = newPort;
                setContentView(createView());
              }
            } catch (RemoteException e) {
              throw new RuntimeException(
                  "WiFiInputMethod failed to connected to HttpService.", e);
            }
          }
          //@Override
          public void onServiceDisconnected(ComponentName name) {
            Debug.d("WiFiInputMethod disconnected from HttpService.");
          }
        }, BIND_AUTO_CREATE) == false) {
          throw new RuntimeException("failed to connect to HttpService");
        }

        setContentView(createView());
    }
    
    private void text(int resId) {
      text(resId, 15);
    }
    
    private void text(int resId, int fontSize) {
      String msg = getString(resId);
      text(msg, fontSize);
    }
    
    private void text(String msg, int fontSize) {
      TextView v = new TextView(this);
      v.setText(msg);
      v.setTextSize(fontSize);
      layout.addView(v);
    }
}