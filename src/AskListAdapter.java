package ru.askor.blagosfera.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Set;

import ru.askor.blagosfera.AskAppConfig;
import ru.askor.blagosfera.R;

/**
 * <p>Класс адаптера для списка устройств</p>
 *
 * @author Shatalin Dmitry
 * @version v89
 */
public class AskListAdapter extends BaseAdapter {
    private final static String TAG = "AskListAdapter";

    private BluetoothDevice[] adevs;
    private LayoutInflater lInflater;

    private boolean isChecked = false;
    private boolean isDisable = false;

    private int selectedItem = -1;

    /**
     * <p>Конструктор</p>
     *
     * @param devices Усройства для отображения
     */
    public AskListAdapter(Set<BluetoothDevice> devices) {
        this.adevs = devices.toArray(new BluetoothDevice[devices.size()]);
        lInflater = (LayoutInflater) AskAppConfig.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return adevs.length;
    }

    @Override
    public Object getItem(int position) {
        return adevs[position];
    }

    /**
     * <p>Получить выделенное устройство</p>
     * @return Выделенное устройство либо null
     */
    public BluetoothDevice getSelectedDevice() {
        if (isChecked && selectedItem != -1) {
            return adevs[selectedItem];
        }
        return null;
    }

    /**
     * <p>Пометить позицию</p>
     *
     * @param checked Пометить truе, снять выделение false
     * @param position Позиция
     */
    public void setChecked(boolean checked, int position) {
        this.isChecked = checked;
        this.selectedItem = position;
        notifyDataSetChanged();
    }

    /**
     * <p>Запретить позицию</p>
     *
     * @param flag Запретить true, разрешить false
     */
    public void setDisable(boolean flag) {
        this.isDisable = flag;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = lInflater.inflate(R.layout.list_adapter_item, parent, false);
            TextView textView = (TextView) view.findViewById(R.id.textView);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView textView = (TextView) v;
                    if (!isDisable) {
                        if (isChecked == false || (isChecked == true && selectedItem == position)) {
                            if (textView.isSelected()) {
                                textView.setTextColor(Color.BLACK);
                                v.setSelected(false);
                                isChecked = false;
                                selectedItem = -1;
                            } else {
                                isChecked = true;
                                selectedItem = position;
                                textView.setTextColor(Color.WHITE);
                                v.setSelected(true);
                            }
                        }
                    } else {
                        textView.setTextColor(Color.BLACK);
                        v.setSelected(false);
                    }
                }
            });
        }


        BluetoothDevice device = (BluetoothDevice) adevs[position];
        if (device != null) {
            ((TextView) view.findViewById(R.id.textView)).setText(device.getName());

            if (isChecked == true && selectedItem == position) {
                ((TextView) view.findViewById(R.id.textView)).setTextColor(Color.WHITE);
            } else {
                ((TextView) view.findViewById(R.id.textView)).setTextColor(Color.BLACK);
            }
        }

        return view;
    }
}
