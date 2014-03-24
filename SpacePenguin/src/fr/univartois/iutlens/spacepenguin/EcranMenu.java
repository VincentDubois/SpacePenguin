package fr.univartois.iutlens.spacepenguin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class EcranMenu extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ecran_menu);
		
		
		//******* BOUTON PLAY ********
        final Button play = (Button) findViewById(R.id.btn_play);
        play.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          Intent intent = new Intent(EcranMenu.this, MainActivity.class);
          startActivity(intent);
          }
        });
        
        //******* BOUTON HIGHSCORE *******
        final Button highscore = (Button) findViewById(R.id.btn_highscore);
        highscore.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          Intent intent = new Intent(EcranMenu.this, EcranMenu.class);
          startActivity(intent);
          }
        });
        
        
        //******* BOUTON CREDITS *******
        final Button credits = (Button) findViewById(R.id.btn_credits);
        credits.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          Intent intent = new Intent(EcranMenu.this, Credits.class);
          startActivity(intent);
          }
        });
        
        //******** BOUTON OPTION *******
        /*final Button option = (Button) findViewById(R.id.btn_option);
        option.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          Intent intent = new Intent(EcranMenu.this, Options.class);
          startActivity(intent);
          }
        });*/
        
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ecran_menu, menu);
		return true;
	}

}
