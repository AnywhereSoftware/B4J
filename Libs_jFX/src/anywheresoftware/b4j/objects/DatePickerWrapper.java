
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package anywheresoftware.b4j.objects;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DatePicker;
import javafx.util.converter.LocalDateStringConverter;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.DateTime;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;

@ShortName("DatePicker")
@DontInheritEvents
@Events(values={"ValueChanged (Value As Long)"})
public class DatePickerWrapper extends ControlWrapper<DatePicker>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new DatePicker());
		super.innerInitialize(ba, eventName, true);
		setDateTicks(DateTime.getNow());
		setDateFormat(DateTime.getDateFormat());
		if (ba.subExists(eventName + "_valuechanged")) {
			getObject().valueProperty().addListener(new ChangeListener<LocalDate>() {

				@Override
				public void changed(
						ObservableValue<? extends LocalDate> observable,
						LocalDate oldValue, LocalDate newValue) {
					ba.raiseEventFromUI(getObject(), eventName + "_valuechanged", LocalDateToLong(newValue));
					
				}
			});
		}
	}
	/**
	 * Sets the date format. The default value is the same as DateTime.DateFormat.
	 */
	public void setDateFormat(String Format) {
		getObject().setConverter(new LocalDateStringConverter(DateTimeFormatter.ofPattern(Format), null));
	}
	/**
	 * Gets or sets the date (as ticks). Pass 0 to clear the value;
	 */
	public long getDateTicks() {
		return LocalDateToLong(getObject().getValue());
	}
	public void setDateTicks(long l) {
		getObject().setValue(l == 0 ? null : LongToLocalDate(l));
	}
	/**
	 * Gets or sets whether the field is editable. The default value is True.
	 */
	public boolean getEditable() {
		return getObject().isEditable();
	}
	public void setEditable(boolean b) {
		getObject().setEditable(b);
	}
	@Hide
	public static long LocalDateToLong(LocalDate ld) {
		if (ld == null)
			return 0;
		return ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}
	@Hide
	public static LocalDate LongToLocalDate(long value) {
		Instant i = Instant.ofEpochMilli(value);
		ZonedDateTime zdt = ZonedDateTime.ofInstant(i, ZoneId.systemDefault());
		return zdt.toLocalDate();
	}
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		DatePicker vg = (DatePicker) prev;
		if (vg == null) {
			vg = NodeWrapper.buildNativeView(DatePicker.class, props, designer);
			vg.setValue(LongToLocalDate(System.currentTimeMillis()));
		}
		vg.setEditable((Boolean)props.get("editable"));
		return ControlWrapper.build(vg, props, designer);
	}
}
