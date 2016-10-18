package ru.askor.blagosfera;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * <p>Базовый абстрактный класс считывателя.
 * Содержит общие методы и данные для связи со считывателем.<p/>
 *
 * @author Shatalin Dmitry
 * @version v54
 */
public abstract class AskDevice {
    private final static String TAG = "AskDevice";

    public final static int CMD_DATA_SIZE = 10240;

    public final static int STATE_NONE       = 500; // Ничего не делать
    public final static int STATE_LISTEN     = 501; // Принимать входящие
    public final static int STATE_CONNECTING = 502; // Установка соединения
    public final static int STATE_CONNECTED  = 503; // Соединение установленно

    public final static int MESSAGE_STATE_CHANGE = 504; //Статус поменялся
    public final static int MESSAGE_READ         = 505; //Читаем
    public final static int MESSAGE_WRITE        = 506; //Пишем
    public final static int MESSAGE_DEVICE_NAME  = 507; //Имя устройства
    public final static int MESSAGE_TOAST        = 508; //Всплывающее сообщение
    public final static int MESSAGE_IMAGE        = 509; //Изображение получено
    public static final int MESSAGE_CONNECT      = 510; //Подсоединиться
    public static final int MESSAGE_DISCONNECT   = 511; //Отсоединиться
    public static final int MESSAGE_GET_IMAGE    = 512; //Получить сканированное изображение

    public final static int SZ_BUFF = 1024; //Размер буфера
    public final static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Уникальный идентификатор

    public final static String P_SERVER_NAME = "bluetooth_server"; //Имя сервера
    public final static String P_DEVICE_NAME = "device_name"; //Имя устройства
    public final static String P_TOAST = "toast"; //Всплывающее сообщение

    protected BluetoothServerSocket serverSocket = null;
    protected BluetoothSocket socket = null;
    protected InputStream inputStream = null;
    protected OutputStream outputStream = null;
    protected final Handler serviceHandler;
    protected volatile boolean runThread = true;

    private int state = STATE_NONE;


    /**
     * <p>Конструктор</p>
     * @param serviceHandler Handler
     */
    public AskDevice(Handler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    /**
     * <p>Вернуть статус</p>
     *
     * @return int
     */
    public int getState() {
        return this.state;
    }

    /**
     * <p>Остановить поток</p>
     */
    public void stopThread() {
        this.runThread = false;
    }

    /**
     * <p>Установить статус считывателя<p/>
     * @param state
     */
    public void setState(int state) {
        this.state = state;
        serviceHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * <p>Удачное соединение<p/>
     *
     * @param deviceName
     */
    public void connectionSuccess(String deviceName) {
        setState(STATE_CONNECTED);

        final Context context = AskAppConfig.getContext();
        final String toastText = String.format(context.getString(R.string.connect_success),
                deviceName);
        serviceHandler.obtainMessage(MESSAGE_TOAST, toastText).sendToTarget();
    }

    /**
     * <p>Соединение не удалось<p/>
     *
     * @param deviceName
     */
    public void connectionFail(String deviceName) {
        setState(STATE_LISTEN);

        final Context context = AskAppConfig.getContext();
        final String toastText = String.format(context.getString(R.string.connect_fail),
                deviceName);
        serviceHandler.obtainMessage(MESSAGE_TOAST, toastText).sendToTarget();
    }
}
