package com.adidas.activityisthekey;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.adidas.activityisthekey.inActivity.InActivityFragment;

/**
 * Display error and retry button
 */

public class ErrorFragment extends Fragment {

    public static final String ARG_MSG = "errorMsg";

    public interface OnErrorFragmentListener {
        void onRetryClick();
    }

    private Button mButton;
    private TextView mErrorText;
    private OnErrorFragmentListener mListener;


    public static Fragment newInstance(String msg){

        ErrorFragment fr = new ErrorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MSG, msg);
        fr.setArguments(args);
        return fr;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof InActivityFragment.OnInActivtyFragmentListener) {
            mListener = (OnErrorFragmentListener) context;
        } else {
            throw new RuntimeException("parent activity must implement OnErrorFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_error, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mErrorText = view.findViewById(R.id.error);
        mErrorText.setText(getArguments().getString(ARG_MSG));

        mButton = view.findViewById(R.id.error_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onRetryClick();
            }
        });

    }
}
