module me.yurinero.hashana {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires com.google.common;
	requires com.fasterxml.jackson.databind;


	opens me.yurinero.hashana to
			javafx.fxml,
			com.fasterxml.jackson.databind,
			javafx.controls;
	exports me.yurinero.hashana;
}