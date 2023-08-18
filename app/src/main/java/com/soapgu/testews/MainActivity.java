package com.soapgu.testews;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.search.CalendarView;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;

public class MainActivity extends AppCompatActivity {
    private CustomExchangeService service;
    TextView tv_msg;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initializeExchange();
        this.tv_msg = findViewById(R.id.tv_msg);
        findViewById(R.id.btn_inbox_item).setOnClickListener( v -> onClickInboxItem());
        findViewById(R.id.btn_appointment).setOnClickListener( v-> onClickAppointments() );

    }

    private void onClickInboxItem(){
        this.disposables.add( bindFolderId(WellKnownFolderName.Inbox)
                .flatMap(this::listFirstTenItems)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( list-> {
                            if( list != null && list.size() > 0 ){
                                String message = String.format("subject:%s", list.get(0).getSubject());
                                this.tv_msg.setText(message);
                            }
                        },
                        throwable -> Toast.makeText(this, throwable.getMessage() , Toast.LENGTH_LONG).show()) );
    }

    private void onClickAppointments(){
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_YEAR,-1);
        Calendar end = Calendar.getInstance();
        end.add(Calendar.DAY_OF_YEAR,1);
        this.disposables.add( this.findAppointmentsRx(start.getTime(),end.getTime())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( appointment->{
                            tv_msg.setText( String.format("名称：%s，时间: %s-%s", appointment.getSubject(), appointment.getStart().toString() , appointment.getEnd().toString()  ) );
                        },
                        throwable -> Toast.makeText(this, throwable.getMessage() , Toast.LENGTH_LONG).show())
                 );
    }

    private Single<FolderId> bindFolderId(WellKnownFolderName name){
        return Single.<FolderId>create( emitter -> {
                    try {
                        Folder inbox = Folder.bind(service, name);
                        emitter.onSuccess( inbox.getId() );
                    } catch (Exception e) {
                        emitter.onError( e );
                    }
                } )
                .subscribeOn(Schedulers.io());
    }

    @SuppressLint("CheckResult")
    private Single<ArrayList<Item>> listFirstTenItems(FolderId folderId ) {

       return Single.create( emitter -> {
            try {
                ItemView view = new ItemView(10);
                FindItemsResults<Item> findResults = service.findItems(folderId,view);
                //MOOOOOOST IMPORTANT: load messages' properties before
                service.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties);
                ArrayList<Item> items = findResults.getItems();
                emitter.onSuccess( items );
            }
            catch (Exception ex){
                emitter.onError(ex);
            }
        } );
    }

    private void initializeExchange() {
        this.service = new CustomExchangeService(ExchangeVersion.Exchange2010_SP2);
        ExchangeCredentials credentials = new WebCredentials("roombooking@shgbit.io", "Shgbit123");
        service.setCredentials(credentials);
        try {
            service.setUrl( new URI("https://172.16.40.75/EWS/exchange.asmx"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Single<Appointment> findAppointmentsRx( Date startDate, Date endDate){
        return Single.<Appointment>create( emitter -> {
            try{
                Appointment appointment = findAppointments(startDate, endDate);
                emitter.onSuccess(appointment);
            }catch (Exception ex){
                emitter.onError(ex);
            }
        }).subscribeOn(Schedulers.io());
    }

    private Appointment findAppointments( Date startDate, Date endDate) throws Exception {
        CalendarFolder folder=CalendarFolder.bind(service, WellKnownFolderName.Calendar);
        FindItemsResults<Appointment> findResults = folder.findAppointments(new CalendarView(startDate, endDate));
        Appointment appt = findResults.getItems().get(0);
        appt.load(PropertySet.FirstClassProperties);
        return appt;
    }
}