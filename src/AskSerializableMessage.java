package ru.askor.blagosfera;

import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * <p>Серелизация сообщений</p>
 *
 * @author Shatalin Dmitry
 * @version v54
 */
public class AskSerializableMessage implements Serializable {
    private static final String TAG = "AskSerializableMessage";

    public static final String ARG1 = "arg1"; //Первый аргумент
    public static final String ARG2 = "arg2"; //Второй аргумент
    public static final String WHAT = "what"; //Тип сообщения
    public static final String OBJ = "obj";   //Объект

    private JSONObject obj = null;

    private static AskSerializableMessage instance = null;

    private AskSerializableMessage() {

    }

    /**
     * <p>Установить сообщение класса</p>
     *
     * @param message Объект Message
     */
    public void setMessage(Message message) {
        this.obj = toJSONObject(message);
    }

    /**
     * <p>Получить сообщение класса</p>
     *
     * @return Объект Message
     */
    public Message getMessage() {
        return fromJSONObject(obj);
    }

    /**
     * <p>Проверка сообщения класса</p>
     *
     * @return Сообщение класса существует? true - Да
     */
    public boolean isMessage() {
        return (this.obj != null);
    }

    /**
     * <p>Получить единственный экземляр класса</p>
     *
     * @return Объект класса AskSerializableMessage
     */
    public static AskSerializableMessage getInstance() {
        if (instance == null) {
            synchronized (AskSerializableMessage.class) {
                if (instance == null) {
                    instance = new AskSerializableMessage();
                }
            }
        }
        return instance;
    }

    /**
     * <p>Запаковка сообщения</p>
     *
     * @param message Объект Message
     * @return Объект JSON
     */
    public JSONObject toJSONObject(Message message) {
        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put(ARG1, message.arg1);
            json.put(ARG2, message.arg2);
            json.put(WHAT, message.what);
            json.put(OBJ, message.obj);
        } catch (JSONException ex) {
            ex.printStackTrace();
            json = null;
        }
        return json;
    }

    /**
     * <p>Распаковка сообщения<p/>
     *
     * @param obj Объект JSON
     * @return Объект Message
     */
    public Message fromJSONObject(JSONObject obj) {
        Message message = null;
        try {
            message = new Message();
            message.arg1 = obj.getInt(ARG1);
            message.arg2 = obj.getInt(ARG2);
            message.what = obj.getInt(WHAT);
            message.obj = obj.get(OBJ);
        } catch (JSONException ex) {
            ex.printStackTrace();
            message = null;
        }
        return message;
    }
}
