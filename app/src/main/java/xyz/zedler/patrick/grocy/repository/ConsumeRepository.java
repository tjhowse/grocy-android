/*
 * This file is part of Grocy Android.
 *
 * Grocy Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Grocy Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Grocy Android. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2020-2021 by Patrick Zedler & Dominic Zedler
 */

package xyz.zedler.patrick.grocy.repository;

import android.app.Application;
import android.os.AsyncTask;

import java.util.ArrayList;

import xyz.zedler.patrick.grocy.database.AppDatabase;
import xyz.zedler.patrick.grocy.model.Product;
import xyz.zedler.patrick.grocy.model.ProductBarcode;
import xyz.zedler.patrick.grocy.model.QuantityUnit;
import xyz.zedler.patrick.grocy.model.QuantityUnitConversion;

public class ConsumeRepository {
    private final AppDatabase appDatabase;

    public ConsumeRepository(Application application) {
        this.appDatabase = AppDatabase.getAppDatabase(application);
    }

    public interface DataListener {
        void actionFinished(
                ArrayList<Product> products,
                ArrayList<ProductBarcode> barcodes,
                ArrayList<QuantityUnit> quantityUnits,
                ArrayList<QuantityUnitConversion> quantityUnitConversions
        );
    }

    public interface DataUpdatedListener {
        void actionFinished();
    }

    public void loadFromDatabase(DataListener listener) {
        new loadAsyncTask(appDatabase, listener).execute();
    }

    private static class loadAsyncTask extends AsyncTask<Void, Void, Void> {
        private final AppDatabase appDatabase;
        private final DataListener listener;

        private ArrayList<Product> products;
        private ArrayList<ProductBarcode> barcodes;
        private ArrayList<QuantityUnit> quantityUnits;
        private ArrayList<QuantityUnitConversion> quantityUnitConversions;


        loadAsyncTask(AppDatabase appDatabase, DataListener listener) {
            this.appDatabase = appDatabase;
            this.listener = listener;
        }

        @Override
        protected final Void doInBackground(Void... params) {
            products = new ArrayList<>(appDatabase.productDao().getAll());
            barcodes = new ArrayList<>(appDatabase.productBarcodeDao().getAll());
            quantityUnits = new ArrayList<>(appDatabase.quantityUnitDao().getAll());
            quantityUnitConversions
                    = new ArrayList<>(appDatabase.quantityUnitConversionDao().getAll());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(listener != null) listener.actionFinished(products, barcodes,
                    quantityUnits, quantityUnitConversions);
        }
    }

    public void updateDatabase(
            ArrayList<Product> products,
            ArrayList<ProductBarcode> barcodes,
            ArrayList<QuantityUnit> quantityUnits,
            ArrayList<QuantityUnitConversion> quantityUnitConversions,
            DataUpdatedListener listener
    ) {
        new updateAsyncTask(
                appDatabase,
                products,
                barcodes,
                quantityUnits,
                quantityUnitConversions,
                listener
        ).execute();
    }

    private static class updateAsyncTask extends AsyncTask<Void, Void, Void> {
        private final AppDatabase appDatabase;
        private final DataUpdatedListener listener;

        private final ArrayList<Product> products;
        private final ArrayList<ProductBarcode> barcodes;
        private final ArrayList<QuantityUnit> quantityUnits;
        private final ArrayList<QuantityUnitConversion> quantityUnitConversions;

        updateAsyncTask(
                AppDatabase appDatabase,
                ArrayList<Product> products,
                ArrayList<ProductBarcode> barcodes,
                ArrayList<QuantityUnit> quantityUnits,
                ArrayList<QuantityUnitConversion> quantityUnitConversions,
                DataUpdatedListener listener
        ) {
            this.appDatabase = appDatabase;
            this.listener = listener;
            this.products = products;
            this.barcodes = barcodes;
            this.quantityUnits = quantityUnits;
            this.quantityUnitConversions = quantityUnitConversions;
        }

        @Override
        protected final Void doInBackground(Void... params) {
            appDatabase.productDao().deleteAll();
            appDatabase.productDao().insertAll(products);
            appDatabase.productBarcodeDao().deleteAll();
            appDatabase.productBarcodeDao().insertAll(barcodes);
            appDatabase.quantityUnitDao().deleteAll();
            appDatabase.quantityUnitDao().insertAll(quantityUnits);
            appDatabase.quantityUnitConversionDao().deleteAll();
            appDatabase.quantityUnitConversionDao().insertAll(quantityUnitConversions);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(listener != null) listener.actionFinished();
        }
    }
}
