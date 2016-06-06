package me.bryanlau.goalbuddiesandroid.Social;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import me.bryanlau.goalbuddiesandroid.Goals.MainGoalFragment;
import me.bryanlau.goalbuddiesandroid.R;
import me.bryanlau.goalbuddiesandroid.Requests.RelationRequest;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainGoalFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainGoalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainSocialFragment extends android.support.v4.app.ListFragment {
    private OnFragmentInteractionListener mListener;
    private int position;
    private ArrayList<String> socialList;
    private ArrayAdapter<String> adapter;

    public MainSocialFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param position The position of the fragment
     * @return A new instance of fragment MainGoalFragment.
     */
    public static MainSocialFragment newInstance(int position) {
        MainSocialFragment fragment = new MainSocialFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        position = getArguments().getInt("position");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_main_social, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        switch (position) {
            case 0:
                socialList = SocialContainer.INSTANCE.friends;
                break;
            case 1:
                socialList = SocialContainer.INSTANCE.incoming;
                break;
            case 2:
                socialList = SocialContainer.INSTANCE.blocked;
                break;
            default:
                socialList = new ArrayList<>();
        }

        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, socialList);
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        String username = socialList.get(position);

        if (SocialContainer.INSTANCE.blocked.contains(username)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.myDialog));

            alert.setTitle(R.string.dialog_username_search_title);
            alert.setMessage(R.string.fragment_social_unblock_message);

            alert.setPositiveButton(
                    getResources().getString(R.string.profile_menu_unblock_friend),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            new RelationRequest(
                                    getActivity(),
                                    socialList.get(position),
                                    RelationRequest.REQUEST_TYPE.UNBLOCK).execute();
                        }
                    });

            alert.setNegativeButton(
                    getResources().getString(R.string.profile_menu_cancel_friend),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });
            alert.show();
        } else {
            Intent i = new Intent(getActivity(), ProfileActivity.class);
            i.putExtra("username", username);
            startActivity(i);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
