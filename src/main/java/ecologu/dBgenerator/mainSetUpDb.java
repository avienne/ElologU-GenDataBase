package ecologu.dBgenerator;



public class mainSetUpDb{

	public static void main (String arg[]){

		dbCreator dbc = new dbCreator();
		System.out.println("Db Creator class loaded");
		dbc.setUpDb();
		System.out.println("Db Creator class Ended");

	}
}



