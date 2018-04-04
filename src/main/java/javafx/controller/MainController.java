package javafx.controller;

import app.HousePriceCalc;
import house_calc_library.additional_classes.Address;
import house_calc_library.exception.ConstructionYearViolationException;
import com.google.maps.errors.ApiException;
import javafx.CustomMessageBox;
import javafx.ListenerMethods;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private WebEngine webEngineMap;
    private ObservableList<Address> addressesObservableList = FXCollections.observableArrayList();
    private CustomMessageBox customMessageBox;

    @FXML
    private Label labelEnterYourData, labelAddress, labelPropertyType, labelMarketType, labelConstructionYear,
            labelNumberOfMeters, labelBuildingType, labelBuildingMaterial, labelResultDistance,
            labelResultBasicPricePerMeter, labelResulFinalPrice, labelResultFinalPricePerMeter, labelReferenceCity,
            labelReferenceCityPricePerMeter;

    @FXML
    private TextField textFieldAddress, textFieldConstructionYear, textFieldNumberOfMeters;

    @FXML
    private ComboBox<String> comboBoxMarketType, comboBoxBuildingType, comboBoxBuildingMaterial;

    @FXML
    private ComboBox<Address> comboBoxAddress;

    @FXML
    private CheckBox checkBoxBalcony, checkBoxCellar, checkBoxGarden, checkBoxTerrace, checkBoxElevator,
            checkBoxSeparateKitchen, checkBoxGuardedEstate;

    @FXML
    private WebView webViewGoogleMaps;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        customMessageBox = new CustomMessageBox("image/icon.png");
        webEngineMap = webViewGoogleMaps.getEngine();
        String url = HousePriceCalc.class.getResource("../map.html").toExternalForm();
        webEngineMap.load(url);

        ListenerMethods listenerMethods = new ListenerMethods();
        textFieldConstructionYear.textProperty().addListener((observable, oldValue, newValue) -> listenerMethods
                .changeLabelTextField("^[1-9]{1}[0-9]{3}$", textFieldConstructionYear, labelConstructionYear,
                        "Podaj rok budowy.", "Niepoprawny format."));

        textFieldNumberOfMeters.textProperty().addListener((observable, oldValue, newValue) -> listenerMethods
                .changeLabelTextField("^[1-9]{1}[0-9]{2}$", textFieldNumberOfMeters, labelNumberOfMeters,
                        "Podaj ilość metrów kw.", "Niepoprawny format."));

        comboBoxAddress.valueProperty().addListener((observable, oldValue, newValue) -> listenerMethods
                .changeLabelComboBox(comboBoxAddress, labelAddress,
                        "Wybierz adres."));

        comboBoxMarketType.valueProperty().addListener((observable, oldValue, newValue) -> listenerMethods
                .changeLabelComboBox(comboBoxMarketType, labelMarketType,
                        "Wybierz rynek."));

        comboBoxBuildingType.valueProperty().addListener((observable, oldValue, newValue) -> listenerMethods
                .changeLabelComboBox(comboBoxBuildingType, labelBuildingType,
                        "Wybierz rodzaj zabudowy."));

        comboBoxBuildingMaterial.valueProperty().addListener((observable, oldValue, newValue) -> listenerMethods
                .changeLabelComboBox(comboBoxBuildingMaterial, labelBuildingMaterial,
                        "Wybierz materiał budynku."));


        ObservableList<String> buildingTypesObservableList
                = FXCollections.observableArrayList(new ArrayList<>(HousePriceCalc.pricesCalculator.getBuildingTypes().keySet()));
        comboBoxBuildingType.setItems(buildingTypesObservableList);
        comboBoxBuildingType.getItems().add(0, "");

        ObservableList<String> marketTypesObservableList
                = FXCollections.observableArrayList(HousePriceCalc.pricesCalculator.getMarketTypes());
        comboBoxMarketType.setItems(marketTypesObservableList);
        comboBoxMarketType.getItems().add(0, "");

        ObservableList<String> buildingMaterialsObservableList
                = FXCollections.observableArrayList(new ArrayList<>(HousePriceCalc.pricesCalculator.getBuildingMaterials().keySet()));
        comboBoxBuildingMaterial.setItems(buildingMaterialsObservableList);
        comboBoxBuildingMaterial.getItems().add(0, "");

        comboBoxAddress.setItems(addressesObservableList);

        resetComponentsValues();
    }

    @FXML
    void buttonCalculate() {
        /*
        webEngineMap.executeScript("clearComponents=true;");
        webEngineMap.executeScript("initMap();");
        */

        /*
        HousePriceCalc.pricesCalculator.autocompleteAddresses(textFieldAddress.getText());
        System.out.println(HousePriceCalc.pricesCalculator.getAutocompleteAddresses()); */

        if (labelAddress.getText().isEmpty() && labelBuildingType.getText().isEmpty()
                && labelMarketType.getText().isEmpty() && labelConstructionYear.getText().isEmpty() &&
                labelNumberOfMeters.getText().isEmpty() && labelBuildingMaterial.getText().isEmpty()) {

            try {
                HousePriceCalc.pricesCalculator.calculateMultiplierForConstructionYear(Integer.valueOf(textFieldConstructionYear.getText()));
            } catch (ConstructionYearViolationException e) {
                customMessageBox.showMessageBox(Alert.AlertType.WARNING, "Ostrzeżenie",
                        "Operacja oszacowania wartości nie powiedzie się.",
                        "Powód: " + e.getCause().getMessage())
                        .showAndWait();
            }
        } else
            customMessageBox.showMessageBox(Alert.AlertType.WARNING, "Ostrzeżenie",
                    "Operacja oszacowania wartości nie powiedzie się.",
                    "Powód: Nie wszystkie wartości mają poprawny format.")
                    .showAndWait();
    }

    @FXML
    void buttonAutocompleteAddresses() {
        try {
            addressesObservableList.clear();
            HousePriceCalc.pricesCalculator.autocompleteAddresses(textFieldAddress.getText());
            List<Address> autocompleteAddresses = HousePriceCalc.pricesCalculator.getAutocompleteAddresses();
            if (autocompleteAddresses != null) {
                addressesObservableList.addAll(autocompleteAddresses);
                webEngineMap.executeScript("drawFlatMarker=false;");
                webEngineMap.executeScript("drawReferenceCity=false;");
            }

        } catch (InterruptedException | ApiException | IOException e) {
            if (e.getCause() != null)
                customMessageBox.showMessageBox(Alert.AlertType.WARNING, "Ostrzeżenie",
                        "Operacja typowania adresów nie powiodła się.",
                        "Powód: " + e.getCause().getMessage() + ".")
                        .showAndWait();
            else
                customMessageBox.showMessageBox(Alert.AlertType.WARNING, "Ostrzeżenie",
                        "Operacja typowania adresów nie powiodła się.",
                        "Powód: " + e.getMessage() + ".")
                        .showAndWait();
        }
    }

    @FXML
    void comboBoxAddress_onAction() {
        if (comboBoxAddress.getSelectionModel().getSelectedItem() != null) {
            HousePriceCalc.pricesCalculator.setSelectedAddress(comboBoxAddress.getSelectionModel().getSelectedItem());

            webEngineMap.executeScript("drawFlatMarker=true;");
            webEngineMap.executeScript("drawReferenceCity=false;");
            webEngineMap.executeScript("yourFlatLat=" + HousePriceCalc.pricesCalculator.getSelectedAddress().getLatitude() + ";");
            webEngineMap.executeScript("yourFlatLng=" + HousePriceCalc.pricesCalculator.getSelectedAddress().getLongitude() + ";");
            webEngineMap.executeScript("initMap();");
        }
    }

    @FXML
    void menuItemModifyGoogleApiKey_onAction() {

    }

    private void resetComponentsValues() {
        textFieldAddress.setText("");
        if (addressesObservableList != null)
            addressesObservableList.clear();
        comboBoxAddress.getSelectionModel().select(0);
        comboBoxMarketType.getSelectionModel().select(0);
        textFieldConstructionYear.setText("");
        textFieldNumberOfMeters.setText("");
        comboBoxBuildingType.getSelectionModel().select(0);
        comboBoxBuildingMaterial.getSelectionModel().select(0);

        checkBoxBalcony.selectedProperty().setValue(false);
        checkBoxCellar.selectedProperty().setValue(false);
        checkBoxGarden.selectedProperty().setValue(false);
        checkBoxTerrace.selectedProperty().setValue(false);
        checkBoxElevator.selectedProperty().setValue(false);
        checkBoxSeparateKitchen.selectedProperty().setValue(false);
        checkBoxGuardedEstate.selectedProperty().setValue(false);

        labelReferenceCity.setText("------");
        labelReferenceCityPricePerMeter.setText("------");
        labelResultDistance.setText("------");
        labelResultBasicPricePerMeter.setText("------");
        labelResulFinalPrice.setText("------");
        labelResultFinalPricePerMeter.setText("------");
    }
}
