package com.example.karokojnr.nadab_hotels;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.karokojnr.nadab_hotels.adapter.OrdersAdapter;
import com.example.karokojnr.nadab_hotels.api.HotelService;
import com.example.karokojnr.nadab_hotels.api.RetrofitInstance;
import com.example.karokojnr.nadab_hotels.model.Order;
import com.example.karokojnr.nadab_hotels.model.OrderItem;
import com.example.karokojnr.nadab_hotels.model.Orders;
import com.example.karokojnr.nadab_hotels.orders.OrderList;
import com.example.karokojnr.nadab_hotels.utils.SharedPrefManager;
import com.example.karokojnr.nadab_hotels.utils.utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OrderFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderFragment extends Fragment {

    private OrdersAdapter adapter;
    private OrderFragment context;
    RecyclerView recyclerView;

    private List<Order> orderLists = new ArrayList<> ();

    public static final String TAG = OrderList.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public OrderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderFragment newInstance(String param1, String param2) {
        OrderFragment fragment = new OrderFragment ();
        Bundle args = new Bundle ();
        args.putString ( ARG_PARAM1, param1 );
        args.putString ( ARG_PARAM2, param2 );
        fragment.setArguments ( args );
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        if (getArguments () != null) {
            mParam1 = getArguments ().getString ( ARG_PARAM1 );
            mParam2 = getArguments ().getString ( ARG_PARAM2 );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate ( R.layout.fragment_order, container, false );
        View view = inflater.inflate ( R.layout.activity_order_list, container, false);
/*
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity ()).setSupportActionBar(toolbar);*/

        context = this;

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult> () {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        utils.sendRegistrationToServer(getActivity (), token);
                        // Log and toast
                        Log.d(TAG, token);
                        Toast.makeText(getActivity (), token, Toast.LENGTH_SHORT).show();
                    }
                });

        /*Create handle for the RetrofitInstance interface*/
        HotelService service = RetrofitInstance.getRetrofitInstance ().create ( HotelService.class );
        Call<Orders> call = service.getOrders( SharedPrefManager.getInstance(getActivity ()).getToken() );
        call.enqueue ( new Callback<Orders> () {
            @Override
            public void onResponse(Call<Orders> call, Response<Orders> response) {
                for (int i = 0; i < response.body ().getOrdersList ().size (); i++) {
                    orderLists.add ( response.body ().getOrdersList().get(i) );
                }
                generateOrdersList ( response.body ().getOrdersList () );
            }

            @Override
            public void onFailure(Call<Orders> call, Throwable t) {
                Log.wtf("LOG", "onFailure: "+t.getMessage() );
                Toast.makeText ( getActivity (), "Something went wrong...Please try later!"+t.getMessage(), Toast.LENGTH_SHORT ).show ();
            }
        } );


        recyclerView = (RecyclerView) view.findViewById ( R.id.orderslist_recycler_view );
        recyclerView.addOnItemTouchListener ( new RecyclerTouchListener( getActivity (), recyclerView, new RecyclerTouchListener.ClickListener () {
            @Override
            public void onClick(View view, final int position) {
                final Order order = orderLists.get ( position );
                OrderItem[] orderItems = order.getOrderItems();
                String items[] = new String[orderItems.length];
                for (int i = 0; i < orderItems.length; i++) {
                    OrderItem item = orderItems[i];
                    items[i] = item.getQty() + " " + item.getName() + " @ Kshs. " + (item.getQty() * item.getPrice());
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity ());
                builder.setTitle("Order items");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getActivity (), "Accepting order No:: "+ order.getOrderId(), Toast.LENGTH_SHORT).show();
                        HotelService service = RetrofitInstance.getRetrofitInstance ().create ( HotelService.class );
                        Call<Order> call = service.acceptOrder(order.getOrderId(), "ACCEPTED");
                        call.enqueue ( new Callback<Order>() {
                            @Override
                            public void onResponse(Call<Order> call, Response<Order> response) {
//                                orderLists.set(position, response.body());
                                adapter.notifyItemChanged(position, response.body());
                            }

                            @Override
                            public void onFailure(Call<Order> call, Throwable t) {
                                Log.wtf("LOG", "onFailure: "+t.getMessage() );
                                Toast.makeText ( getActivity (), "Something went wrong...Please try later!"+t.getMessage(), Toast.LENGTH_SHORT ).show ();
                            }
                        } );
                    }
                });
                builder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.setNegativeButton("REJECT", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity (), "Rejecting order No:: "+ order.getOrderId(), Toast.LENGTH_SHORT).show();
                        HotelService service = RetrofitInstance.getRetrofitInstance ().create ( HotelService.class );
                        Call<Order> call = service.acceptOrder(order.getOrderId(), "REJECTED");
                        call.enqueue ( new Callback<Order>() {
                            @Override
                            public void onResponse(Call<Order> call, Response<Order> response) {
                                orderLists.add(response.body());
                                adapter.notifyDataSetChanged();
//                                adapter.notifyItemChanged(position, response.body());
                            }

                            @Override
                            public void onFailure(Call<Order> call, Throwable t) {
                                Toast.makeText ( getActivity (), "Something went wrong...Please try later!"+t.getMessage(), Toast.LENGTH_SHORT ).show ();
                            }
                        } );
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        } ) );


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction ( uri );
        }
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach ( context );
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException ( context.toString () + " must implement OnFragmentInteractionListener" );
        }
    }*/

    @Override
    public void onDetach() {
        super.onDetach ();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void generateOrdersList(ArrayList<Order> empDataList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager ( getActivity () );

        adapter = new OrdersAdapter( empDataList, getActivity ());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter (adapter);
    }
}