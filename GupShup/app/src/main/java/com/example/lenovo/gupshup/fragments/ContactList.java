package com.example.lenovo.gupshup.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.lenovo.gupshup.Model.ContactData;
import com.example.lenovo.gupshup.R;
import com.example.lenovo.gupshup.RecyclerViewDecoration;
import com.example.lenovo.gupshup.activities.AboutUs;
import com.example.lenovo.gupshup.activities.MainActivity;
import com.example.lenovo.gupshup.adapters.ContactAdapter;
import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.TouchScrollBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * A fragment representing a list of Items.
 */
public class ContactList extends Fragment implements MainActivity.OnBackPressedListener {

    private SearchView search;
    private static final String TAG = "ContactList";
    private static ContactList ctctList;
    private Cursor phoneList;
    private ContactAdapter contactAdapter;
    private RecyclerView recyclerView;
    private ArrayList<ContactData> nameList = new ArrayList<>();
    private SearchView.OnQueryTextListener listener;
    private static final String[] reqPerm = {
            Manifest.permission.READ_CONTACTS
    };
    public static final int reqCode = 1080;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactList() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ContactList getInstance() {
        if (ctctList == null) {
            ctctList = new ContactList();
        }
        return ctctList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).setBackListenerForFrag2(this);
        ////////////////////////
        listener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                Log.d(TAG, "onQueryTextChange: execute triggered for" + newText);
                new SearchContact().execute(newText.toLowerCase());
                return true;
            }
        };
        /////////////////////////
        contactAdapter = new ContactAdapter(nameList, getActivity());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionRequest();
        } else new GetContacts().execute();
        Log.d(TAG, "onCreate: contact");

    }
    @SuppressLint("NewApi")
    private void permissionRequest() {
        int permCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS);
        switch (permCheck) {
            case PackageManager.PERMISSION_GRANTED:
                new GetContacts().execute();
                break;
            case PackageManager.PERMISSION_DENIED:
                ActivityCompat.requestPermissions(getActivity(),reqPerm,reqCode);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==reqCode && grantResults.length>0 && permissions[0].equals(reqPerm[0])) {
            new GetContacts().execute();
        } else
            Toast.makeText(getActivity(), "Contact functionality disabled", Toast.LENGTH_SHORT).show();
    }

    private void getContacts() {
        phoneList = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        String tmpPh="";
        assert phoneList != null;
        while (phoneList.moveToNext()) {
            String name = phoneList.getString(phoneList.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phoneList.getString(phoneList.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (phone.length() >= 10 && !tmpPh.equals(phone)) {
                nameList.add(new ContactData(name, phone));
                tmpPh=phone;
            }
        }
        phoneList.close();
        Collections.sort(nameList, new Comparator<ContactData>() {
            @Override
            public int compare(ContactData lhs, ContactData rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_list_layout, container, false);

        Log.d(TAG, "onCreateView: contacts");
        // Set the adapter

        recyclerView = (RecyclerView) view.findViewById(R.id.list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Drawable divider = ContextCompat.getDrawable(getActivity(), R.drawable.my_divider);
        recyclerView.addItemDecoration(new RecyclerViewDecoration(divider));
        recyclerView.setAdapter(contactAdapter);

        TouchScrollBar scrollBar = (TouchScrollBar) view.findViewById(R.id.touch_scroll);
        scrollBar.setHandleColour(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        scrollBar.addIndicator(new AlphabetIndicator(getActivity()), false);
        scrollBar.setRecyclerView(recyclerView);
        scrollBar.setHideDuration(500);
        return view;
    }

    @Override
    public boolean doBack() {
        if (search != null && !search.isIconified()) {
            search.onActionViewCollapsed();
            return true;
        }
        return false;
    }

    class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            getContacts();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            contactAdapter.notifyDataSetChanged();

        }
    }
    //TODO: should have used viewpager's menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.srch_actn);
        if (item != null) {
            search = (SearchView) item.getActionView();
        }
        Log.d(TAG, "onCreateOptionsMenu: " + search.toString());
        search.setQueryHint("Enter name");
        search.setOnQueryTextListener(listener);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        search.setOnQueryTextListener(listener);
        switch (item.getItemId()) {
            case R.id.srch_actn:
                return true;
            case R.id.about:
                startActivity(new Intent(getActivity(), AboutUs.class));
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    class SearchContact extends AsyncTask<String, Void, Void> {

        private ArrayList<ContactData> filteredData = new ArrayList<>();

        @Override
        protected Void doInBackground(String... params) {

            for (int i = 0; i < nameList.size(); i++) {
                String text = nameList.get(i).getName().toLowerCase();
                Log.d(TAG, "doInBackground: Name" + text);
                if (text.contains(params[0])) filteredData.add(nameList.get(i));
            }
            Log.d(TAG, "doInBackground: fds" + filteredData.size());

            if (!params[0].isEmpty()) {
                contactAdapter = new ContactAdapter(filteredData, getActivity());
            } else {
                contactAdapter = new ContactAdapter(nameList, getActivity());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            recyclerView.setAdapter(contactAdapter);
            contactAdapter.notifyDataSetChanged();
        }
    }

}
