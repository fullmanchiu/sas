package cn.colafans.sas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppConfigAdapter extends RecyclerView.Adapter<AppConfigAdapter.ViewHolder> {
    private List<AppInfo> mAppList;
    private Context mContext;

    private OnItemCheckedChangeListener mOnItemCheckedChangeListener;

    public interface OnItemCheckedChangeListener {
        void onItemCheckedChange(CompoundButton buttonView, int position, boolean isChecked);
    }

    public void setOnItemCheckedChangeListener(OnItemCheckedChangeListener mOnItemCheckedChangeListener) {
        this.mOnItemCheckedChangeListener = mOnItemCheckedChangeListener;
    }

    public AppConfigAdapter(Context context, List<AppInfo> appList) {
        mContext = context;
        mAppList = appList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.app_config_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo appInfo = mAppList.get(position);
        holder.switchConfig.setChecked(appInfo.isFullScreen);
        holder.appIcon.setImageDrawable(appInfo.icon);
        holder.appName.setText(appInfo.appName);
    }

    @Override
    public int getItemCount() {
        return mAppList == null ? 0 : mAppList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView appIcon;
        public TextView appName;
        public Switch switchConfig;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.app_icon);
            appName = itemView.findViewById(R.id.app_name);
            switchConfig = itemView.findViewById(R.id.switch_config);
            switchConfig.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (mOnItemCheckedChangeListener != null) {
                    mOnItemCheckedChangeListener.onItemCheckedChange(buttonView, getAdapterPosition(), isChecked);
                }
            });
        }
    }

}
