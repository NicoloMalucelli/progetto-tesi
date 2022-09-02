package test;

import View.DTManagerView;
import controller.Controller;

public class DigitalTwinManager {

	public static void main(String[] args) {
		DTManagerView view = new DTManagerView();
		Controller controller = new Controller();
		controller.setView(view);
		view.setController(controller);
	}

}
