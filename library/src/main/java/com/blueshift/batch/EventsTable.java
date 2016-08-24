package com.blueshift.batch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.blueshift.framework.BaseSqliteTable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rahul on 24/8/16.
 */
public class EventsTable extends BaseSqliteTable<Event> {

    public static final String TABLE_NAME = "events";

    private static final String LOG_TAG = "EventsTable";
    private static final String FIELD_ID = "id";
    private static final String FIELD_EVENT_PARAMS_JSON = "event_params_json";

    private static EventsTable sInstance;

    public EventsTable(Context context) {
        super(context);
    }

    public static EventsTable getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new EventsTable(context);
        }

        return sInstance;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected Event loadObject(Cursor cursor) {
        Event event = new Event();

        if (cursor != null && !cursor.isAfterLast()) {
            event.setId(cursor.getLong(cursor.getColumnIndex(FIELD_ID)));
            event.setEventParamsJson(cursor.getString(cursor.getColumnIndex(FIELD_EVENT_PARAMS_JSON)));
        }

        return event;
    }

    @Override
    protected ContentValues getContentValues(Event event) {
        ContentValues contentValues = new ContentValues();

        if (event != null) {
            contentValues.put(FIELD_EVENT_PARAMS_JSON, event.getEventParamsJson());
        }

        return contentValues;
    }

    @Override
    protected HashMap<String, FieldType> getFields() {
        HashMap<String, FieldType> fieldTypeHashMap = new HashMap<>();

        fieldTypeHashMap.put(FIELD_ID, FieldType.Autoincrement);
        fieldTypeHashMap.put(FIELD_EVENT_PARAMS_JSON, FieldType.String);

        return fieldTypeHashMap;
    }

    @Override
    protected Long getId(Event event) {
        return event != null ? event.getId() : 0;
    }

    /**
     * Bulk event api takes max 100 events (for now. the actual limit is max 1MB).
     * This method is used for generating an array of event request parameter JSONs.
     * This method will retrieve and delete the first 100 records from db. Then will create
     * an ArrayList of Strings, where each element will be on event request's parameters as JSON.
     *
     * @return array of event request parameter JSONs
     */
    public ArrayList<String> getBulkEventParameters() {
        ArrayList<String> result = new ArrayList<>();

        ArrayList<Event> events = findWithLimit(100);
        if (events.size() > 0) {
            ArrayList<String> idList = new ArrayList<>();

            for (Event event : events) {
                // get the event parameter
                result.add(event.getEventParamsJson());

                // get id for deleting the item.
                idList.add(String.valueOf(event.getId()));
            }

            int count = delete(FIELD_ID, idList);
            Log.d(LOG_TAG, "Deleted " + count + " events.");
        }

        return result;
    }
}
