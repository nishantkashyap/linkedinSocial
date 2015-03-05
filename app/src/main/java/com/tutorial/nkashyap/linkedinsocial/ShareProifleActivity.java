package com.tutorial.nkashyap.linkedinsocial;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.brickred.socialauth.Career;
import org.brickred.socialauth.Position;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthAdapter.Provider;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;


public class ShareProifleActivity extends Activity {

    SocialAuthAdapter adapter;
    Career careerMap;
    Position[] pos;
    String uName ;
    String uTitle;
    String uCompanyName;
    String uEmail;
    String uMobile;
    String uAddress;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_linkedin_profile);

        careerMap = (Career) getIntent().getSerializableExtra("career");

        Button fetchLinkedinDetailsButton = (Button) findViewById(R.id.getDetails);

        ImageButton imageButton = (ImageButton)findViewById(R.id.imageButton);

        imageButton.setBackgroundResource(R.drawable.linkedin);


        adapter = new SocialAuthAdapter(new ResponseListener());

        // Add provider
        adapter.addProvider(Provider.LINKEDIN, R.drawable.linkedin);


        // Add key and Secret
        try {
            adapter.addConfig(Provider.LINKEDIN, "75dsc3mcgi2jto", "byNtmlwgeIgmR70L", null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        fetchLinkedinDetailsButton.setOnClickListener(new OnClickListener()
        {

            public void onClick(View v)
            {

                adapter.authorize(ShareProifleActivity.this,Provider.LINKEDIN);

            }


        });


    }

    private final class ResponseListener implements DialogListener{

        @Override
        public void onComplete(Bundle values) {

            adapter.getUserProfileAsync(new ProfileDataListener());
            adapter.getCareerAsync(new CareerListener());
        }
        @Override
        public void onCancel(){

        }
        @Override
        public void onBack(){

        }

        @Override
        public void onError(SocialAuthError error){

        }
    }


    private final class  ProfileDataListener implements SocialAuthListener<Profile>{

        @Override
        public void onExecute(String e , Profile p){

            Profile profileMap = p;


            Log.d("InCard",  "Validate ID         = " + profileMap.getValidatedId());
            Log.d("InCard",  "First Name          = " + profileMap.getFirstName());
            Log.d("InCard",  "Last Name           = " + profileMap.getLastName());
            Log.d("InCard",  "Email               = " + profileMap.getEmail());
            Log.d("InCard",  "Country                  = " + profileMap.getCountry());
            Log.d("InCard",  "Language                 = " + profileMap.getLanguage());
            Log.d("InCard",  "Contact Info                 = " + profileMap.getContactInfo().get("home"));
            Log.d("InCard",  "Profile Image URL  = " + profileMap.getProfileImageURL());


            TextView name = (TextView) findViewById(R.id.name);
            TextView email = (TextView) findViewById(R.id.email);
            TextView mainAddress = (TextView) findViewById(R.id.mainAddress);
            TextView contact = (TextView) findViewById(R.id.contact);


            uName = profileMap.getFirstName().toString() + " " + profileMap.getLastName().toString();
            uEmail = profileMap.getEmail().toString();
            uAddress = profileMap.getContactInfo().get("mainAddress");
            uMobile = profileMap.getContactInfo().get("home");

            new DownloadImageTask((ImageView) findViewById(R.id.profileImage))
                    .execute(profileMap.getProfileImageURL().toString());
            name.setText(uName);
            email.setText(uEmail);
            mainAddress.setText(uAddress);
            contact.setText(uMobile);

        }

        @Override
        public void onError(SocialAuthError error){

        }
    }


    // To receive the feed response after authentication
    private final class CareerListener implements SocialAuthListener<Career> {

        @Override
        public void onExecute(String provider, Career t) {

            Log.d("InCard", "Receiving Data");
            Career careerMap = t;

            if (careerMap.getPositions() != null && careerMap.getPositions().length > 0) {
                pos = careerMap.getPositions();

            }

            TextView companyName = (TextView) findViewById(R.id.companyName);
            TextView title = (TextView) findViewById(R.id.title);

            Log.d("InCard",  "Company Name         = " + pos[0].getCompanyName().toString());
            Log.d("InCard",  "Title        = " + pos[0].getTitle().toString());

            uTitle = pos[0].getTitle().toString();
            uCompanyName = pos[0].getCompanyName().toString();

            companyName.setText(uCompanyName);
            title.setText(uTitle);


        }

        @Override
        public void onError(SocialAuthError e) {
        }
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public void onClickWhatsApp(View view) {

        createVisitingCard();


        try {

            Intent whatsAppIntent = new Intent(Intent.ACTION_SEND);
            whatsAppIntent.setType("image/jpeg");
            whatsAppIntent.setPackage("com.whatsapp");

            whatsAppIntent.putExtra(Intent.EXTRA_STREAM,
                    Uri.parse(Environment.getExternalStorageDirectory() + File.separator + "incard.jpg"));


            startActivity(Intent.createChooser(whatsAppIntent, "Share your inCard with"));

        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    public void createVisitingCard(){

        Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.incard); // the original file yourimage.jpg i added in resources
        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);

        String name = "Name: "+uName;
        String title = "Title: " +uTitle;
        String company = "Company: "+uCompanyName;
        String contact = "Mobile: "+uMobile;
        String address = "Address: "+uAddress;


        Canvas cs = new Canvas(dest);
        Paint tPaint = new Paint();
        tPaint.setTextSize(18);
        tPaint.setColor(Color.BLACK);
        tPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        cs.drawBitmap(src, 0f, 0f, null);
        float height = tPaint.measureText("yY");
        float width = tPaint.measureText(name);
        float x_coord = (src.getWidth() - width)/2;

        Paint tPaintImage = new Paint();
        Bitmap bitmapImage = BitmapFactory.decodeResource(getResources(),
                R.drawable.linkedin);

        cs.drawBitmap(getResizedBitmap(bitmapImage,35,30),05f,height+05f,tPaintImage);

        cs.drawText(name, x_coord, height + 100f, tPaint);
        cs.drawText(title, x_coord, height+124f, tPaint);
        cs.drawText(company, x_coord, height+148f, tPaint);
        cs.drawText(contact, x_coord, height+172f, tPaint);
        cs.drawText(address, x_coord, height+196f, tPaint);
        try {
            dest.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(Environment.getExternalStorageDirectory().getPath() +"/" +"incard.jpg")));
            // dest is Bitmap, if you want to preview the final image, you can display it on screen also before saving
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }



}