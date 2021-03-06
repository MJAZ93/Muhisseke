package com.firedevz.sistemadegestaofinanceira.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.firedevz.sistemadegestaofinanceira.R;
import com.firedevz.sistemadegestaofinanceira.adapter.ListaRendimentosAdapter;
import com.firedevz.sistemadegestaofinanceira.modelo.Rendimento;
import com.firedevz.sistemadegestaofinanceira.sql.DatabaseHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RendimentosActivity extends AppCompatActivity {

    //private TextView mTextMessage;

    EditText edtDescricao, edtValor, edtData;
    Button btnLimpar, btnSalvar, btnCExcluir;
    ListView lstViewRendimentos;
    Spinner spnTipo;
    private DatePickerDialog.OnDateSetListener mDateSetListener1;
    private static final String TAG ="RendimentosActivity";
    private String data1;


    DatabaseHelper db = new DatabaseHelper(this);

    private List<Rendimento> listaRendimentos = new ArrayList<>();
    private ListaRendimentosAdapter listaRendimentosAdapter;
    private RecyclerView recyclerView;

    ArrayAdapter<String> adapter;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adpOpcoes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rendimentos);

        edtDescricao = (EditText) findViewById(R.id.edtDescricao);
        edtValor = (EditText) findViewById(R.id.edtValor);
        edtData = (EditText) findViewById(R.id.edtData);
        spnTipo = (Spinner) findViewById(R.id.spnTipo);
        btnLimpar = (Button) findViewById(R.id.btnLimpar);
        btnSalvar = (Button) findViewById(R.id.btnSalvar);
        btnCExcluir = (Button) findViewById(R.id.btnExcluir);
        recyclerView = (RecyclerView) findViewById(R.id.recycle_lista_rend);
        final Spinner spnContaAdicionar = (Spinner) findViewById(R.id.spnContaAdicionar);

        String[] spinnerLists = db.getAllSpinnerAccounts();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(RendimentosActivity.this,android.R.layout.simple_spinner_item, spinnerLists);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnContaAdicionar.setAdapter(spinnerAdapter);


        listaRendimentosAdapter = new ListaRendimentosAdapter(listaRendimentos);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(listaRendimentosAdapter);

        listaRendimentos();

//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//        edtData.setText(dateFormat.format(new Date()));


        edtData.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(RendimentosActivity.this,android.R.style.Theme_Holo_Light_Dialog_MinWidth,mDateSetListener1,year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener1 = new DatePickerDialog.OnDateSetListener(){
            @SuppressLint("LongLogTag")
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                month = month+1;
                Log.d(TAG, ": data "+dayOfMonth+"/"+month+"/"+year);
                data1 = dayOfMonth+"/"+month+"/"+year;
                edtData.setText(data1);

            }
        };




        btnLimpar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limpaCampos();
            }
        });


        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String descricao = edtDescricao.getText().toString();
                float valor = Float.parseFloat(edtValor.getText().toString());
                String tipo = spnTipo.getSelectedItem().toString();
                String data = edtData.getText().toString();
                String contaAdicionar = spnContaAdicionar.getSelectedItem().toString();
                float valorRendimentoConta=0;

                if (descricao.isEmpty()) {
                    edtDescricao.setError("campo Obrigatorio");
                    edtValor.setError("campo Obrigatorio");
                } else {
                    //insert
                    db.addRendimetno(new Rendimento(descricao, valor,tipo, data,contaAdicionar));
                    Toast.makeText(RendimentosActivity.this, "Rendimento adicionado com Sucesso", Toast.LENGTH_LONG).show();
                    valorRendimentoConta=db.somaRendimentoConta(contaAdicionar);
                    Toast.makeText(RendimentosActivity.this, "Adicionado a conta: "+contaAdicionar+" "+valorRendimentoConta+"0 MZN", Toast.LENGTH_SHORT).show();
                    db.adicionaNaConta(contaAdicionar,valorRendimentoConta);
                    listaRendimentos();
                    limpaCampos();

                }
            }
        });




        btnCExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String descricao = edtDescricao.getText().toString();

                if (descricao.isEmpty()) {
                    Toast.makeText(RendimentosActivity.this, "Selecione uma Rendimento", Toast.LENGTH_LONG).show();
                } else {
                    Rendimento rendimento = new Rendimento();
                    rendimento.setDescricao(descricao);
                    db.apagarRendimento(rendimento);

                    Toast.makeText(RendimentosActivity.this, "Rendimento Excluido com Sucesso", Toast.LENGTH_LONG).show();
                    limpaCampos();
                    listaRendimentos();

                }

            }
        });


        adpOpcoes = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        adpOpcoes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTipo.setAdapter(adpOpcoes);

        adpOpcoes.add("Empresariais");
        adpOpcoes.add("Profissionais");
        adpOpcoes.add("Renda");
        adpOpcoes.add("Juro");
        adpOpcoes.add("Lucro");
        adpOpcoes.add("Incrementos Patrimoniais e indemnizações");
        adpOpcoes.add("Pensão");
        adpOpcoes.add("Outro");
        adpOpcoes.add("Outro");


    }


    void limpaCampos() {
        edtData.setText("");
        edtDescricao.setText("");
        edtValor.setText("");
        edtDescricao.requestFocus();
    }

//    public void listaRendimentos() {
//        List<Rendimento> rendimentos = db.listaTodosRendimentos();
//
//        arrayList = new ArrayList<String>();
//
//        adapter = new ArrayAdapter<String>(RendimentosActivity.this, android.R.layout.simple_list_item_1,arrayList);
//
//        lstViewRendimentos.setAdapter(adapter);
//
//        for(Rendimento c: rendimentos){
//            // Log.d("Lista","\nID: " + c.getCodigo() + "Nome: "+c.getNome());
//            arrayList.add(c.getDescricao()+ "       "+c.getValor()+ "       "+c.getData());
//            adapter.notifyDataSetChanged();
//
//        }
//    }


    public void listaRendimentos() {

        Cursor dados = db.listaTodosRendimentos();

        if (dados.getCount() == 0) {
            Toast.makeText(this, "Nenhum Rendimento Adicionado", Toast.LENGTH_LONG).show();
        } else {
            while (dados.moveToNext()) {
                String descricao = dados.getString(1);
                float valor = Float.parseFloat(dados.getString(2));
                String tipo = dados.getString(3);
                String data = dados.getString(4);

                Rendimento listaiten = new Rendimento(descricao, valor, tipo, data);
                listaRendimentos.add(listaiten);
            }
        }


    }
}
