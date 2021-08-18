package iitp.naman.mksdrive;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by naman on 14-12-2017.
 * Single row depicting folder
 */

class AdapterFolderView extends BaseAdapter {
    private final String[] file_Id;
    private final String[] file_mimeType;
    private final String[] file_name;
    private final String[] file_size;
    private final LayoutInflater inflater;

    AdapterFolderView(Context context, String[] file_Id,String[] file_mimeType,String[] file_name,String[] file_size) {
        this.file_Id=file_Id;
        this.file_mimeType=file_mimeType;
        this.file_name=file_name;
        this.file_size=file_size;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View gridView;
        if (convertView == null) {
            gridView = inflater.inflate(R.layout.single_row_folderview, parent, false);
        }
        else {
            gridView =  convertView;
        }
        if(file_mimeType[position].equalsIgnoreCase("application/vnd.google-apps.folder")){
            gridView.findViewById(R.id.single_row_folderview_item).setBackground(parent.getResources().getDrawable(R.drawable.entity_box_folder));
        }
        else{
            gridView.findViewById(R.id.single_row_folderview_item).setBackground(parent.getResources().getDrawable(R.drawable.entity_box));
        }
        ((TextView) gridView.findViewById(R.id.folderName)).setText(file_name[position]);
        ((TextView) gridView.findViewById(R.id.folderId)).setText(file_Id[position]);
        ((TextView) gridView.findViewById(R.id.folderMimeType)).setText(file_mimeType[position]);
        ((TextView) gridView.findViewById(R.id.folderSize)).setText(file_size[position]);
        return gridView;
    }

    @Override
    public int getCount() {
        return file_Id.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}

