package ru.askor.blagosfera;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ru.askor.blagosfera.interfaces.AskCallback;

/**
 * <p>Класс помощник для Bluetooth устройств</p>
 *
 * @author Shatalin Dmitry
 * @version v77
 */
public class AskBluetoothHelper {
    private final static String TAG = "AskBluetoothHelper";

    private static final int BT_DISCOVERABLE_DURATION = 1100;
    private static List<BluetoothDevice> devices = null; //Обнаруженные устройства для подключенния
    private BluetoothAdapter ba;
    private static Activity activity = null;
//    private IntentFilter filterFound;
//    private IntentFilter filterStart;
//    private IntentFilter filterFinish;

    /**
     * <p>Нашел Bluetooth устройство</p>
     */
    private static final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && devices != null) {
                    Toast.makeText(activity, "FOUND DEVICE\n" + device.getName(), Toast.LENGTH_SHORT).show();
                    devices.add(device);
                }
                return;
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                if (devices != null) {
                    Toast.makeText(activity, "DISCOVERY_STARTED\n", Toast.LENGTH_SHORT).show();
                    devices.clear();
                }
                return;
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (listener != null) {
                    Toast.makeText(activity, "DISCOVERY_FINISHED\n", Toast.LENGTH_SHORT).show();
                    listener.callbackFunc();
                }
                return;
            }
        }
    };

    private static AskCallback listener = null;

    public void setListener(AskCallback listener) {
        this.listener = listener;
    }

    public AskBluetoothHelper(Activity activity) {
        this.activity = activity;

        ba = BluetoothAdapter.getDefaultAdapter();
        devices = new ArrayList<BluetoothDevice>();

//        if (receiver != null && activity != null) {
//            filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//            this.activity.registerReceiver(receiver, filterFound);
//            filterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//            this.activity.registerReceiver(receiver, filterStart);
//            filterFinish = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//            this.activity.registerReceiver(receiver, filterFinish);
//        }
    }

    /**
     * <p>Отписатся от поиска новых устройств</p>
     */
    public void close() {
        /*activity.unregisterReceiver(receiver);*/
    }

    /**
     * <p>Сделать устройство доступным для поиска других устройств</p>
     */
    public void makeDiscoverable() {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                BT_DISCOVERABLE_DURATION);
        activity.startActivityForResult(discoverableIntent, 0);
    }

    /**
     * <p>Получить список сопряженных устройств</p>
     *
     * @return Set<BluetoothDevice>
     */
    public Set<BluetoothDevice> getBoundedDevices() {
        return ba.getBondedDevices();
    }

    /**
     * <p>Получить список сопряженых устройств в формате Имя + Адрес</p>
     *
     * @return Map<name, address>
     */
    public HashMap<String, String> getBoundedNameAddress() {
        Set<BluetoothDevice> pairedDevices = ba.getBondedDevices();
        HashMap map = new HashMap<String, String>();

        for (BluetoothDevice bt : pairedDevices) {
            map.put(bt.getName(), bt.getAddress());
        }
        return map;
    }

    /**
     * <p>Выдает список имен и адресов заново обнаруженных устройств</>
     *
     * @return Map<name, address>
     */
    public HashMap<String, String> getDevicesNameAddress() {
        if (devices.size() == 0) {
            return null;
        }

        HashMap map = new HashMap<String, String>();
        for (BluetoothDevice bt : devices) {
            map.put(bt.getName(), bt.getAddress());
        }
        return map;
    }

    /**
     * <p>Выдает список типов BluetoothDevice обнаруженных устройств<p/>
     *
     * @return List<BluetoothDevice>
     */
    public List<BluetoothDevice> getDevices() {
        return this.devices;
    }

    /**
     * <p>Возвращает устройство по uuid</>
     *
     * @param uuid Уникальный идентификатор устройства
     * @return BluetoothDevice
     */
    public BluetoothDevice getDevice(String uuid) {
        Set<BluetoothDevice> pairedDevices = ba.getBondedDevices();
        for (BluetoothDevice bt : pairedDevices) {
            if (uuid.equals(bt.getAddress())) {
                return bt;
            }
        }
        return null;
    }

    /**
     * <p>Старт обнаружения устройств<p/>
     */
    public void startDiscovery() {
        if (ba.isDiscovering()) {
            ba.cancelDiscovery();
        }
        ba.startDiscovery();
        return;
    }

    /**
     * <p>Сменить имя устройства</>
     *
     * @param name Новое имя устройства
     */
    public boolean changeDeviceName(String name) {
        return ba.setName(name);
    }

    /**
     * <p>Запрос на получение прав<p/>
     *
     * @param constant Запрашиваемый флаг
     */
    public void askEnableBlt(int constant) {
        if (!ba.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableIntent, constant);
        }
    }

    /**
     * <p>Включить Bluetooth</p>
     */
    public void turnOn() {
        if (ba == null) {
            return;
        }
        boolean b = false;
        while (ba.isEnabled() == false) {
            if (b == false) {
                ba.enable();
                b = true;
            }
            SystemClock.sleep(150);
        }
    }

    /**
     * <p>Вернуть текущее имя Bluetooth</p>
     *
     * @return Текущее имя  Bluetooth адаптера
     */
    public String getName() {
        return ba.getName();
    }

    /**
     * <p>Вернуть текущий адрес Bluetooth адаптера</p>
     *
     * @return Текущий адрес Bluetooth адаптера
     */
    public String getAddress() {
        return ba.getAddress();
    }

    /**
     * <p>Вернуть адаптер</p>
     *
     * @return  BluetoothAdapter
     */
    public BluetoothAdapter getAdapter() {
        return ba;
    }

    /**
     * <p>Выключить Bluetooth</p>
     */
    public void turnOff() {
        if (ba == null) {
            return;
        }
        if (ba.isEnabled()) {
            ba.disable();
        }
    }

    /**
     * <p>Состояние(Включен/Выключен) Bluetooth</p>
     *
     * @return Включен true
     */
    public boolean isEnabled() {
        return ba.isEnabled();
    }
}