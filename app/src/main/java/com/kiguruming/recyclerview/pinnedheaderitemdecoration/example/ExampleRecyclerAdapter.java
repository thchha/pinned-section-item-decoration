package com.kiguruming.recyclerview.pinnedheaderitemdecoration.example;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kiguruming.recyclerview.itemdecoration.PinnedHeaderItemDecoration;

import java.util.ArrayList;

/**
 * @author takahr@gmail.com
 */
public class ExampleRecyclerAdapter extends RecyclerView.Adapter<ExampleRecyclerAdapter.ExampleViewHolder> implements PinnedHeaderItemDecoration.PinnedHeaderAdapter {
    public static class Item {
        public final static int TYPE_SECTION = 0;
        public final static int TYPE_DATA = 1;
        public final static int TYPE_FOOTER = 2;

        int type;
        String label;

        public Item(int type, String label) {
            this.type = type;
            this.label = label;
        }
    }

    static abstract class ExampleViewHolder extends RecyclerView.ViewHolder {

        private final int mViewType;

        public ExampleViewHolder(View itemView, int viewType) {
            super(itemView);
            mViewType = viewType;
        }

        public static ExampleViewHolder createViewHolder(ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View root;

            switch (viewType) {
                case Item.TYPE_SECTION:
                    root = inflater.inflate(R.layout.adapter_example_section, parent, false);
                    return new SectionViewHolder(root, viewType);

                case Item.TYPE_DATA:
                    root = inflater.inflate(R.layout.adapter_example_data, parent, false);
                    return new DataViewHolder(root, viewType);

                case Item.TYPE_FOOTER:
                    root = inflater.inflate(R.layout.adapter_example_footer, parent, false);
                    return new FooterViewHolder(root, viewType);

                default:
                    return null;
            }
        }

        abstract public void bindItem(ExampleRecyclerAdapter adapter, Item item, int position);
    }

    static class SectionViewHolder extends ExampleViewHolder {
        private TextView mSectionLabel;

        public SectionViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
            mSectionLabel = (TextView) itemView.findViewById(R.id.section_label);
        }

        @Override
        public void bindItem(ExampleRecyclerAdapter adapter, Item item, int position) {
            mSectionLabel.setText(item.label);
        }
    }

    static class DataViewHolder extends ExampleViewHolder {
        private TextView mDataLabel;

        public DataViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
            mDataLabel = (TextView) itemView.findViewById(R.id.data_label);
        }

        @Override
        public void bindItem(ExampleRecyclerAdapter adapter, Item item, int position) {
            mDataLabel.setText(item.label);
        }
    }

    static class FooterViewHolder extends ExampleViewHolder {
        private Button mFooterButton;

        public FooterViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
            mFooterButton = (Button) itemView.findViewById(R.id.footer_button);
        }

        @Override
        public void bindItem(final ExampleRecyclerAdapter adapter, final Item item, final int position) {
            mFooterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.onClickFooterButton(v, item);
                }
            });
        }
    }

    interface OnClickAddDataListener {
        void onClickAddData(View v, int position);
    }

    private OnClickAddDataListener mOnClickAddDataListener = null;

    ArrayList<Item> mItems = new ArrayList<Item>();

    @Override
    public ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ExampleViewHolder.createViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(ExampleViewHolder viewHolder, int position) {
        final Item item = mItems.get(position);
        viewHolder.bindItem(this, item, position);
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).type;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addSection() {
        final int insertPosition = mItems.size();
        mItems.add(new Item(Item.TYPE_SECTION, String.format("Section at %d", insertPosition)));
        mItems.add(new Item(Item.TYPE_FOOTER, "Footer"));
        notifyItemRangeInserted(insertPosition, 2);
    }

    private void addData(int position) {
        mItems.add(position, new Item(Item.TYPE_DATA, "Data" + position));
        notifyItemInserted(position);
    }

    @Override
    public boolean isPinnedViewType(int viewType) {
        if (viewType == Item.TYPE_SECTION) {
            return true;
        } else {
            return false;
        }
    }

    private void setOnClickAddDataListener(OnClickAddDataListener listener) {
        mOnClickAddDataListener = listener;
    }

    private void onClickFooterButton(View v, Item item) {
        final int position = mItems.indexOf(item);

        if (mOnClickAddDataListener != null) {
            mOnClickAddDataListener.onClickAddData(v, position);
        }

        addData(position);
    }

}
