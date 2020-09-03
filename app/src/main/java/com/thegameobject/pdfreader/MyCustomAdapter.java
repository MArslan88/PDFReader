package com.thegameobject.pdfreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyCustomAdapter extends ArrayAdapter<FileNameModel> {

    private Context context;
    private List<FileNameModel> nameModelsList;
    private List<FileNameModel> nameModelsListFiltered;

    public MyCustomAdapter( Context context, List<FileNameModel> nameModelsList) {
        super(context, R.layout.adapter_pdf,nameModelsList);
        this.context = context;
        this.nameModelsList = nameModelsList;
        this.nameModelsListFiltered = nameModelsList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_pdf,null,true);
        TextView tvFileName = view.findViewById(R.id.tv_name);
        ImageView imageView = view.findViewById(R.id.iv_image);

        tvFileName.setText(nameModelsListFiltered.get(position).getFileName());
//        Glide.with(context).load(nameModelsListFiltered.get(position).getFlag()).into(imageView);

        return view;
    }

    @Override
    public int getCount() {
        return nameModelsListFiltered.size();
    }

    @Nullable
    @Override
    public FileNameModel getItem(int position) {
        return nameModelsListFiltered.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults filterResults = new FilterResults();
                if(constraint == null || constraint.length() == 0){
                    filterResults.count = nameModelsList.size();
                    filterResults.values = nameModelsList;

                }else{
                    List<FileNameModel> resultsModel = new ArrayList<>();
                    String searchStr = constraint.toString().toLowerCase();

                    for(FileNameModel itemsModel:nameModelsList){
                        if(itemsModel.getFileName().toLowerCase().contains(searchStr)){
                            resultsModel.add(itemsModel);

                        }
                        filterResults.count = resultsModel.size();
                        filterResults.values = resultsModel;
                    }


                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                nameModelsListFiltered = (List<FileNameModel>) results.values;
//                MainActivity.nameModelsList = (List<FileNameModel>) results.values;
                notifyDataSetChanged();

            }
        };
        return filter;
    }
}
