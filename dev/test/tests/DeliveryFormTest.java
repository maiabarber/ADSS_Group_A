package tests;

import domain.DeliveryForm;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryFormTest {

    private DeliveryForm createFormWithMeasurements() {
        return new DeliveryForm(new ArrayList<>(List.of(5000.0, 7200.5, 8100.0)));
    }

    @Test
    void defaultConstructor_createsEmptyFormSuccessfully() {
        DeliveryForm form = new DeliveryForm();

        assertNotNull(form);
        assertTrue(form.getWeightMeasurements().isEmpty());
        assertFalse(form.hasMeasurements());
    }

    @Test
    void constructor_validMeasurements_createsFormSuccessfully() {
        DeliveryForm form = createFormWithMeasurements();

        assertNotNull(form);
        assertEquals(3, form.getWeightMeasurements().size());
        assertTrue(form.hasMeasurements());
    }

    @Test
    void constructor_nullMeasurements_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryForm(null)
        );
    }

    @Test
    void constructor_withNullMeasurementInsideList_throwsException() {
        List<Double> measurements = new ArrayList<>();
        measurements.add(5000.0);
        measurements.add(null);

        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryForm(measurements)
        );
    }

    @Test
    void constructor_withNegativeMeasurementInsideList_throwsException() {
        List<Double> measurements = new ArrayList<>();
        measurements.add(5000.0);
        measurements.add(-1.0);

        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryForm(measurements)
        );
    }

    @Test
    void constructor_copiesInputListAndDoesNotKeepOriginalReference() {
        List<Double> originalMeasurements = new ArrayList<>();
        originalMeasurements.add(5000.0);
        originalMeasurements.add(7000.0);

        DeliveryForm form = new DeliveryForm(originalMeasurements);

        originalMeasurements.add(9000.0);

        assertEquals(2, form.getWeightMeasurements().size());
    }

    @Test
    void getWeightMeasurements_returnsCopyAndNotOriginalList() {
        DeliveryForm form = createFormWithMeasurements();

        List<Double> measurementsFromGetter = form.getWeightMeasurements();
        measurementsFromGetter.add(9999.0);

        assertEquals(3, form.getWeightMeasurements().size());
    }

    @Test
    void addWeightMeasurement_validMeasurement_addsMeasurementSuccessfully() {
        DeliveryForm form = new DeliveryForm();

        form.addWeightMeasurement(6500.0);

        assertEquals(1, form.getWeightMeasurements().size());
        assertEquals(6500.0, form.getLatestWeightMeasurement());
        assertTrue(form.hasMeasurements());
    }

    @Test
    void addWeightMeasurement_zeroMeasurement_addsMeasurementSuccessfully() {
        DeliveryForm form = new DeliveryForm();

        form.addWeightMeasurement(0.0);

        assertEquals(1, form.getWeightMeasurements().size());
        assertEquals(0.0, form.getLatestWeightMeasurement());
    }

    @Test
    void addWeightMeasurement_negativeMeasurement_throwsException() {
        DeliveryForm form = new DeliveryForm();

        assertThrows(IllegalArgumentException.class, () ->
                form.addWeightMeasurement(-1.0)
        );
    }

    @Test
    void getLatestWeightMeasurement_whenFormIsEmpty_throwsException() {
        DeliveryForm form = new DeliveryForm();

        assertThrows(IllegalStateException.class, form::getLatestWeightMeasurement);
    }

    @Test
    void getLatestWeightMeasurement_whenFormHasMeasurements_returnsLastMeasurement() {
        DeliveryForm form = createFormWithMeasurements();

        assertEquals(8100.0, form.getLatestWeightMeasurement());
    }

    @Test
    void hasMeasurements_whenFormIsEmpty_returnsFalse() {
        DeliveryForm form = new DeliveryForm();

        assertFalse(form.hasMeasurements());
    }

    @Test
    void hasMeasurements_whenFormHasMeasurements_returnsTrue() {
        DeliveryForm form = createFormWithMeasurements();

        assertTrue(form.hasMeasurements());
    }
}