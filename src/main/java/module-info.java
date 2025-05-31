module me.yurinero.hashana {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires com.google.common;
	requires com.fasterxml.jackson.databind;
	requires java.desktop;
	requires org.slf4j;


	opens me.yurinero.hashana to
			javafx.fxml,
			com.fasterxml.jackson.databind,
			javafx.controls;
	exports me.yurinero.hashana;
	exports me.yurinero.hashana.controllers;
	opens me.yurinero.hashana.controllers to com.fasterxml.jackson.databind, javafx.controls, javafx.fxml;
	exports me.yurinero.hashana.utils;
	opens me.yurinero.hashana.utils to com.fasterxml.jackson.databind, javafx.controls, javafx.fxml;
}