// Generated by view binder compiler. Do not edit!
package xyz.prathamgandhi.copx.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import xyz.prathamgandhi.copx.R;

public final class SigninPopupWindowBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final EditText firstNameSignin;

  @NonNull
  public final EditText lastNameSignin;

  @NonNull
  public final LinearLayout linearLayout2;

  @NonNull
  public final EditText passwordSignin;

  @NonNull
  public final EditText phoneSignin;

  @NonNull
  public final Button register;

  @NonNull
  public final Button submitforsignin;

  private SigninPopupWindowBinding(@NonNull ConstraintLayout rootView,
      @NonNull EditText firstNameSignin, @NonNull EditText lastNameSignin,
      @NonNull LinearLayout linearLayout2, @NonNull EditText passwordSignin,
      @NonNull EditText phoneSignin, @NonNull Button register, @NonNull Button submitforsignin) {
    this.rootView = rootView;
    this.firstNameSignin = firstNameSignin;
    this.lastNameSignin = lastNameSignin;
    this.linearLayout2 = linearLayout2;
    this.passwordSignin = passwordSignin;
    this.phoneSignin = phoneSignin;
    this.register = register;
    this.submitforsignin = submitforsignin;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static SigninPopupWindowBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static SigninPopupWindowBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.signin_popup_window, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static SigninPopupWindowBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.first_name_signin;
      EditText firstNameSignin = rootView.findViewById(id);
      if (firstNameSignin == null) {
        break missingId;
      }

      id = R.id.last_name_signin;
      EditText lastNameSignin = rootView.findViewById(id);
      if (lastNameSignin == null) {
        break missingId;
      }

      id = R.id.linearLayout2;
      LinearLayout linearLayout2 = rootView.findViewById(id);
      if (linearLayout2 == null) {
        break missingId;
      }

      id = R.id.password_signin;
      EditText passwordSignin = rootView.findViewById(id);
      if (passwordSignin == null) {
        break missingId;
      }

      id = R.id.phone_signin;
      EditText phoneSignin = rootView.findViewById(id);
      if (phoneSignin == null) {
        break missingId;
      }

      id = R.id.register;
      Button register = rootView.findViewById(id);
      if (register == null) {
        break missingId;
      }

      id = R.id.submitforsignin;
      Button submitforsignin = rootView.findViewById(id);
      if (submitforsignin == null) {
        break missingId;
      }

      return new SigninPopupWindowBinding((ConstraintLayout) rootView, firstNameSignin,
          lastNameSignin, linearLayout2, passwordSignin, phoneSignin, register, submitforsignin);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
