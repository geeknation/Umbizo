package garden.stick.umbizo.umbizo;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class PedInfo extends ActionBarActivity {
    Intent I= new Intent();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ped_info);

        String pedName=I.getStringExtra("PED_NAME");
        String pedPhone=I.getStringExtra("PED_PHONE");
        String pedLocation=I.getStringExtra("PED_LOCATION");

        TextView pedPhoneView=(TextView) findViewById(R.id.pedPhone);
        TextView pedNameView=(TextView) findViewById(R.id.pedName);
        TextView pedLocationView=(TextView) findViewById(R.id.pedLocation);

        pedPhoneView.setText(pedPhone);
        pedNameView.setText(pedPhone);
        pedLocationView.setText(pedLocation);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_garden, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
