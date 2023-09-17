package com.example.holidayplannerapp.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.holidayplannerapp.Database.Database;
import com.example.holidayplannerapp.R;
import com.google.android.material.textfield.TextInputEditText;


public class userUpdateFragment extends Fragment {
    private TextInputEditText first_name_et_update_user, last_name_et_update_user;
    private TextInputEditText email_et_update_user, password_et_update_user, cm_password_update_user;
    private Button save_btn_update_user, back_btn_update_user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_update, container, false);

        // Initialize views and buttons
        first_name_et_update_user = view.findViewById(R.id.first_name_et_update_user);
        last_name_et_update_user = view.findViewById(R.id.last_name_et_update_user);
        email_et_update_user = view.findViewById(R.id.email_et_update_user);
        password_et_update_user = view.findViewById(R.id.password_et_update_user);
        cm_password_update_user = view.findViewById(R.id.cm_password_update_user);
        save_btn_update_user = view.findViewById(R.id.save_btn_update_user);
        back_btn_update_user = view.findViewById(R.id.back_btn_update_user);

        // Inside your fragment's onCreateView or any method
        Bundle args = getArguments();
        if (args != null) {
            String userEmail = args.getString("user_email");
            email_et_update_user.setText(userEmail);
        }

        // Set click listener for the Save button
        save_btn_update_user.setOnClickListener(v -> updateUser());

        // Set click listener for the Back button
        back_btn_update_user.setOnClickListener(v -> replaceFragment(new UserFragment()));

        return view;
    }


    // Define this function in your activity or fragment
    private void replaceFragment(Fragment newFragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.f_container, newFragment);
        fragmentTransaction.commit();
    }

    private void updateUser() {
        String fname = first_name_et_update_user.getText().toString().trim();
        String lname = last_name_et_update_user.getText().toString().trim(); // Corrected to use last name
        String email = email_et_update_user.getText().toString().trim();
        String pass = password_et_update_user.getText().toString().trim();
        String cmPass = cm_password_update_user.getText().toString().trim();

        if (email.isEmpty() || fname.isEmpty() || pass.isEmpty() || cmPass.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
        } else if (!pass.equals(cmPass)) {
            Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
        } else {
            Database db = new Database(getContext());
            boolean updateSuccessful = db.updateUserDataByEmail(email, fname, lname, pass);

            if (updateSuccessful) {
                Toast.makeText(getActivity(), "Update Successful", Toast.LENGTH_SHORT).show();
                updateEmail(email);
                replaceFragment(new UserFragment());
            } else {
                Toast.makeText(getActivity(), "Update Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void updateEmail(String newEmail) {
        // Use getActivity() to access the context and getSharedPreferences
        SharedPreferences sp = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("email", newEmail);
        editor.apply();
    }
}
