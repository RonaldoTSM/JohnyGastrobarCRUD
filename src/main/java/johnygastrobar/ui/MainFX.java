package johnygastrobar.ui;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import johnygastrobar.dao.FuncionarioDAO;
import johnygastrobar.model.Funcionario;
import johnygastrobar.dao.MesaDAO;
import johnygastrobar.model.Mesa;
import johnygastrobar.dao.ItemDAO;
import johnygastrobar.model.Item;
import johnygastrobar.dao.ReservaDAO;
import johnygastrobar.model.Reserva;
import johnygastrobar.dao.PedidoDAO;
import johnygastrobar.model.Pedido;
import johnygastrobar.model.Pedido.PedidoItem;
import johnygastrobar.dao.PagamentoDAO;
import johnygastrobar.model.Pagamento;

import johnygastrobar.util.ConnectionFactory;

// Imports java.sql.* explicitamente para evitar 'cannot resolve symbol'
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class MainFX extends Application {

    private final FuncionarioDAO funcionarioDao = new FuncionarioDAO();
    private final ObservableList<Funcionario> funcionarios = FXCollections.observableArrayList();
    private final ObservableList<Funcionario> garconsDisponiveis = FXCollections.observableArrayList();
    private final ObservableList<Funcionario> gerentesDisponiveis = FXCollections.observableArrayList();
    private TableView<Funcionario> funcionarioTable;

    private final MesaDAO mesaDao = new MesaDAO();
    private final ObservableList<Mesa> mesas = FXCollections.observableArrayList();
    private final ObservableList<Mesa> mesasDisponiveis = FXCollections.observableArrayList();
    private TableView<Mesa> mesaTable;

    private final ItemDAO itemDao = new ItemDAO();
    private final ObservableList<Item> itens = FXCollections.observableArrayList();
    private final ObservableList<Item> itensDisponiveis = FXCollections.observableArrayList();
    private TableView<Item> itemTable;

    private final ReservaDAO reservaDao = new ReservaDAO();
    private final ObservableList<Reserva> reservas = FXCollections.observableArrayList();
    private TableView<Reserva> reservaTable;

    private final PedidoDAO pedidoDao = new PedidoDAO();
    private final ObservableList<Pedido> pedidos = FXCollections.observableArrayList();
    private final ObservableList<Pedido> pedidosNaoPagos = FXCollections.observableArrayList();
    private TableView<Pedido> pedidoTable;
    private final ObservableList<PedidoItem> itensPedidoAtual = FXCollections.observableArrayList();
    private TableView<PedidoItem> itensPedidoTable;
    // NOVO: Variável de instância para o CheckBox de filtro de pedidos
    private CheckBox mostrarPagosCheckBox;

    private final PagamentoDAO pagamentoDao = new PagamentoDAO();
    private final ObservableList<Pagamento> pagamentos = FXCollections.observableArrayList();
    private TableView<Pagamento> pagamentoTable;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Johny Gastrobar");

        TabPane tabPane = new TabPane();

        // --- Aba de Funcionários ---
        Tab funcionarioTab = new Tab("Funcionários");
        funcionarioTab.setClosable(false);
        VBox funcionarioLayout = createFuncionarioTabContent();
        funcionarioTab.setContent(funcionarioLayout);
        tabPane.getTabs().add(funcionarioTab);

        // --- Aba de Mesas ---
        Tab mesaTab = new Tab("Mesas");
        mesaTab.setClosable(false);
        VBox mesaLayout = createMesaTabContent();
        mesaTab.setContent(mesaLayout);
        tabPane.getTabs().add(mesaTab);

        // --- Aba de Itens ---
        Tab itemTab = new Tab("Itens");
        itemTab.setClosable(false);
        VBox itemLayout = createItemTabContent();
        itemTab.setContent(itemLayout);
        tabPane.getTabs().add(itemTab);

        // --- Aba de Reservas ---
        Tab reservaTab = new Tab("Reservas");
        reservaTab.setClosable(false);
        VBox reservaLayout = createReservaTabContent();
        reservaTab.setContent(reservaLayout);
        tabPane.getTabs().add(reservaTab);

        // --- Aba de Pedidos ---
        Tab pedidoTab = new Tab("Pedidos");
        pedidoTab.setClosable(false);
        VBox pedidoLayout = createPedidoTabContent();
        pedidoTab.setContent(pedidoLayout);
        tabPane.getTabs().add(pedidoTab);

        // --- Aba de Pagamentos ---
        Tab pagamentoTab = new Tab("Pagamentos");
        pagamentoTab.setClosable(false);
        VBox pagamentoLayout = createPagamentoTabContent();
        pagamentoTab.setContent(pagamentoLayout);
        tabPane.getTabs().add(pagamentoTab);


        VBox mainLayout = new VBox(tabPane);
        Scene scene = new Scene(mainLayout, 1200, 700);
        stage.setScene(scene);
        stage.show();

        // Carrega as tabelas ao iniciar a aplicação
        carregarTabelaFuncionarios();
        carregarTabelaMesas();
        carregarTabelaItens();
        carregarTabelaReservas();
        carregarMesasDisponiveis();
        carregarFuncionariosDisponiveis();
        carregarItensDisponiveis();
        carregarTabelaPedidos(); // Carrega os pedidos (inicialmente não pagos)
        carregarPedidosNaoPagos(); // Carrega pedidos para o ComboBox de Pagamento
        carregarTabelaPagamentos();
    }

    // --- Métodos para criar o conteúdo das Abas ---

    // Aba de Funcionários (mantida, com chamadas DAO com conexão)
    private VBox createFuncionarioTabContent() {
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(20));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome do Funcionário");
        TextField cpfField = new TextField();
        cpfField.setPromptText("CPF (somente números)");
        TextField salarioField = new TextField();
        salarioField.setPromptText("Salário (Ex: 3500.00)");
        ComboBox<String> tipoBox = new ComboBox<>();
        tipoBox.getItems().addAll("Garçom", "Cozinheiro", "Bartender", "Gerente");
        tipoBox.setPromptText("Selecione o Tipo");

        Button btnInserir = new Button("Inserir Funcionário");
        btnInserir.setOnAction(e -> {
            Connection conn = null; // A conexão para a transação
            try {
                conn = ConnectionFactory.getConnection();
                conn.setAutoCommit(false); // Inicia a transação
                try {
                    String nome = nomeField.getText();
                    String cpf = cpfField.getText();
                    double salario = Double.parseDouble(salarioField.getText());
                    String tipo = tipoBox.getValue();

                    if (nome.isEmpty() || cpf.isEmpty() || tipo == null || salarioField.getText().isEmpty()) {
                        showAlert("Erro de Validação", "Por favor, preencha todos os campos e selecione o tipo.", Alert.AlertType.WARNING);
                        return;
                    }

                    int novoId = funcionarioDao.inserir(new Funcionario(nome, cpf, salario), conn);

                    if (novoId > 0) {
                        inserirEspecializacao(novoId, tipo, conn); // Passa a conexão
                        conn.commit();
                        showAlert("Sucesso", "Funcionário e tipo inseridos com sucesso!", Alert.AlertType.INFORMATION);
                        nomeField.clear();
                        cpfField.clear();
                        salarioField.clear();
                        tipoBox.getSelectionModel().clearSelection();
                        carregarTabelaFuncionarios();
                        carregarFuncionariosDisponiveis();
                    } else {
                        conn.rollback();
                        showAlert("Erro", "Falha ao inserir funcionário.", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException nfe) {
                    conn.rollback();
                    showAlert("Erro de Entrada", "Salário deve ser um número válido (Ex: 3500.00).", Alert.AlertType.ERROR);
                } catch (SQLException ex) {
                    conn.rollback();
                    showAlert("Erro no Banco de Dados", "Erro ao inserir funcionário: " + ex.getMessage(), Alert.AlertType.ERROR);
                } catch (Exception ex) {
                    conn.rollback();
                    showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            } catch (SQLException ex) {
                showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
            } finally {
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (SQLException ex) {
                    showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        formGrid.add(new Label("Nome:"), 0, 0);
        formGrid.add(nomeField, 1, 0);
        formGrid.add(new Label("CPF:"), 0, 1);
        formGrid.add(cpfField, 1, 1);
        formGrid.add(new Label("Salário:"), 2, 0);
        formGrid.add(salarioField, 3, 0);
        formGrid.add(new Label("Tipo:"), 2, 1);
        formGrid.add(tipoBox, 3, 1);
        GridPane.setColumnSpan(btnInserir, 4);
        formGrid.add(btnInserir, 0, 2);

        funcionarioTable = new TableView<>();
        TableColumn<Funcionario, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));
        colId.setPrefWidth(50);

        TableColumn<Funcionario, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNome()));
        colNome.setPrefWidth(200);

        TableColumn<Funcionario, String> colCpf = new TableColumn<>("CPF");
        colCpf.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCpf()));
        colCpf.setPrefWidth(120);

        TableColumn<Funcionario, Number> colSalario = new TableColumn<>("Salário");
        colSalario.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getSalario()));
        colSalario.setPrefWidth(100);

        funcionarioTable.getColumns().addAll(colId, colNome, colCpf, colSalario);
        funcionarioTable.setItems(funcionarios);

        TextField novoSalarioField = new TextField();
        novoSalarioField.setPromptText("Novo Salário");
        novoSalarioField.setPrefWidth(120);

        Button btnAtualizar = new Button("Atualizar Salário");
        btnAtualizar.setOnAction(e -> {
            Funcionario f = funcionarioTable.getSelectionModel().getSelectedItem();
            if (f != null && !novoSalarioField.getText().isEmpty()) {
                Connection conn = null;
                try {
                    conn = ConnectionFactory.getConnection();
                    conn.setAutoCommit(false);
                    try {
                        double novoSalario = Double.parseDouble(novoSalarioField.getText());
                        funcionarioDao.atualizarSalario(f.getId(), novoSalario, conn);
                        conn.commit();
                        showAlert("Sucesso", "Salário atualizado com sucesso!", Alert.AlertType.INFORMATION);
                        novoSalarioField.clear();
                        carregarTabelaFuncionarios();
                        carregarFuncionariosDisponiveis();
                    } catch (NumberFormatException nfe) {
                        conn.rollback();
                        showAlert("Erro de Entrada", "Novo salário deve ser um número válido.", Alert.AlertType.ERROR);
                    } catch (SQLException ex) {
                        conn.rollback();
                        showAlert("Erro no Banco de Dados", "Erro ao atualizar salário: " + ex.getMessage(), Alert.AlertType.ERROR);
                    } catch (Exception ex) {
                        conn.rollback();
                        showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                } catch (SQLException ex) {
                    showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
                } finally {
                    try {
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            conn.close();
                        }
                    } catch (SQLException ex) {
                        showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            } else {
                showAlert("Atenção", "Selecione um funcionário e digite um novo salário.", Alert.AlertType.WARNING);
            }
        });

        Button btnDeletar = new Button("Deletar Funcionário");
        btnDeletar.setOnAction(e -> {
            Funcionario f = funcionarioTable.getSelectionModel().getSelectedItem();
            if (f != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja deletar " + f.getNome() + "?", ButtonType.YES, ButtonType.NO);
                confirmAlert.setHeaderText("Confirmar Deleção");
                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        Connection conn = null;
                        try {
                            conn = ConnectionFactory.getConnection();
                            conn.setAutoCommit(false);
                            try {
                                funcionarioDao.deletar(f.getId(), conn);
                                conn.commit();
                                showAlert("Sucesso", "Funcionário deletado com sucesso!", Alert.AlertType.INFORMATION);
                                carregarTabelaFuncionarios();
                                carregarFuncionariosDisponiveis();
                            } catch (SQLException ex) {
                                conn.rollback();
                                showAlert("Erro no Banco de Dados", "Erro ao deletar funcionário: " + ex.getMessage(), Alert.AlertType.ERROR);
                            } catch (Exception ex) {
                                conn.rollback();
                                showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        } catch (SQLException ex) {
                            showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
                        } finally {
                            try {
                                if (conn != null) {
                                    conn.setAutoCommit(true);
                                    conn.close();
                                }
                            } catch (SQLException ex) {
                                showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        }
                    }
                });
            } else {
                showAlert("Atenção", "Selecione um funcionário para deletar.", Alert.AlertType.WARNING);
            }
        });

        Button btnRecarregar = new Button("Recarregar Tabela");
        btnRecarregar.setOnAction(e -> carregarTabelaFuncionarios());

        HBox acoes = new HBox(15);
        acoes.setPadding(new Insets(10, 20, 10, 20));
        acoes.getChildren().addAll(new Label("Salário:"), novoSalarioField, btnAtualizar, btnDeletar, btnRecarregar);
        acoes.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(formGrid, funcionarioTable, acoes);
        return layout;
    }

    // Aba de Mesas (mantida, com chamadas DAO com conexão)
    private VBox createMesaTabContent() {
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(20));

        TextField capacidadeField = new TextField();
        capacidadeField.setPromptText("Capacidade (número de pessoas)");
        TextField localizacaoField = new TextField();
        localizacaoField.setPromptText("Localização (Ex: Varanda, Interna)");

        Button btnInserirMesa = new Button("Inserir Mesa");
        btnInserirMesa.setOnAction(e -> {
            Connection conn = null;
            try {
                conn = ConnectionFactory.getConnection();
                conn.setAutoCommit(false);
                try {
                    int capacidade = Integer.parseInt(capacidadeField.getText());
                    String localizacao = localizacaoField.getText();

                    if (localizacao.isEmpty() || capacidade <= 0) {
                        showAlert("Erro de Validação", "Preencha a localização e uma capacidade válida (> 0).", Alert.AlertType.WARNING);
                        return;
                    }

                    Mesa novaMesa = new Mesa(capacidade, localizacao);
                    int idGerado = mesaDao.inserir(novaMesa, conn); // Passa a conexão

                    if (idGerado > 0) {
                        conn.commit();
                        showAlert("Sucesso", "Mesa inserida com ID: " + idGerado, Alert.AlertType.INFORMATION);
                        capacidadeField.clear();
                        localizacaoField.clear();
                        carregarTabelaMesas();
                        carregarMesasDisponiveis();
                    } else {
                        conn.rollback();
                        showAlert("Erro", "Falha ao inserir mesa.", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException nfe) {
                    conn.rollback();
                    showAlert("Erro de Entrada", "Capacidade deve ser um número inteiro válido.", Alert.AlertType.ERROR);
                } catch (SQLException ex) {
                    conn.rollback();
                    showAlert("Erro no Banco de Dados", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                } catch (Exception ex) {
                    conn.rollback();
                    showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            } catch (SQLException ex) {
                showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
            } finally {
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (SQLException ex) {
                    showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        formGrid.add(new Label("Capacidade:"), 0, 0);
        formGrid.add(capacidadeField, 1, 0);
        formGrid.add(new Label("Localização:"), 0, 1);
        formGrid.add(localizacaoField, 1, 1);
        GridPane.setColumnSpan(btnInserirMesa, 2);
        formGrid.add(btnInserirMesa, 0, 2);


        mesaTable = new TableView<>();
        TableColumn<Mesa, Number> colMesaId = new TableColumn<>("ID");
        colMesaId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdMesa()));
        colMesaId.setPrefWidth(50);

        TableColumn<Mesa, Number> colMesaCapacidade = new TableColumn<>("Capacidade");
        colMesaCapacidade.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCapacidade()));
        colMesaCapacidade.setPrefWidth(100);

        TableColumn<Mesa, String> colMesaLocalizacao = new TableColumn<>("Localização");
        colMesaLocalizacao.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocalizacao()));
        colMesaLocalizacao.setPrefWidth(200);

        mesaTable.getColumns().addAll(colMesaId, colMesaCapacidade, colMesaLocalizacao);
        mesaTable.setItems(mesas);

        Button btnAtualizarMesa = new Button("Atualizar Mesa");
        btnAtualizarMesa.setOnAction(e -> {
            Mesa mesaSelecionada = mesaTable.getSelectionModel().getSelectedItem();
            if (mesaSelecionada != null) {
                Connection conn = null;
                try {
                    conn = ConnectionFactory.getConnection();
                    conn.setAutoCommit(false);
                    try {
                        int novaCapacidade = Integer.parseInt(capacidadeField.getText());
                        String novaLocalizacao = localizacaoField.getText();

                        if (novaLocalizacao.isEmpty() || novaCapacidade <= 0) {
                            showAlert("Erro de Validação", "Preencha a nova localização e uma capacidade válida (> 0).", Alert.AlertType.WARNING);
                            return;
                        }

                        mesaSelecionada.setCapacidade(novaCapacidade);
                        mesaSelecionada.setLocalizacao(novaLocalizacao);
                        mesaDao.atualizar(mesaSelecionada, conn); // Passa a conexão
                        conn.commit();
                        showAlert("Sucesso", "Mesa atualizada com sucesso!", Alert.AlertType.INFORMATION);
                        capacidadeField.clear();
                        localizacaoField.clear();
                        carregarTabelaMesas();
                        carregarMesasDisponiveis();
                    } catch (NumberFormatException nfe) {
                        conn.rollback();
                        showAlert("Erro de Entrada", "Capacidade deve ser um número inteiro válido.", Alert.AlertType.ERROR);
                    } catch (SQLException ex) {
                        conn.rollback();
                        showAlert("Erro no Banco de Dados", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                    } catch (Exception ex) {
                        conn.rollback();
                        showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                } catch (SQLException ex) {
                    showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
                } finally {
                    try {
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            conn.close();
                        }
                    } catch (SQLException ex) {
                        showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            } else {
                showAlert("Atenção", "Selecione uma mesa na tabela para atualizar.", Alert.AlertType.WARNING);
            }
        });

        Button btnDeletarMesa = new Button("Deletar Mesa");
        btnDeletarMesa.setOnAction(e -> {
            Mesa mesaSelecionada = mesaTable.getSelectionModel().getSelectedItem();
            if (mesaSelecionada != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja deletar a Mesa ID: " + mesaSelecionada.getIdMesa() + "?", ButtonType.YES, ButtonType.NO);
                confirmAlert.setHeaderText("Confirmar Deleção");
                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        Connection conn = null;
                        try {
                            conn = ConnectionFactory.getConnection();
                            conn.setAutoCommit(false);
                            try {
                                mesaDao.deletar(mesaSelecionada.getIdMesa(), conn); // Passa a conexão
                                conn.commit();
                                showAlert("Sucesso", "Mesa deletada com sucesso!", Alert.AlertType.INFORMATION);
                                carregarTabelaMesas();
                                carregarMesasDisponiveis();
                            } catch (SQLException ex) {
                                conn.rollback();
                                showAlert("Erro no Banco de Dados", "Erro ao deletar mesa: " + ex.getMessage(), Alert.AlertType.ERROR);
                            } catch (Exception ex) {
                                conn.rollback();
                                showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        } catch (SQLException ex) {
                            showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
                        } finally {
                            try {
                                if (conn != null) {
                                    conn.setAutoCommit(true);
                                    conn.close();
                                }
                            } catch (SQLException ex) {
                                showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        }
                    }
                });
            } else {
                showAlert("Atenção", "Selecione uma mesa para deletar.", Alert.AlertType.WARNING);
            }
        });

        Button btnRecarregarMesa = new Button("Recarregar Mesas");
        btnRecarregarMesa.setOnAction(e -> carregarTabelaMesas());

        HBox acoesMesa = new HBox(15);
        acoesMesa.setPadding(new Insets(10, 20, 10, 20));
        acoesMesa.getChildren().addAll(new Label("Capacidade:"), capacidadeField, new Label("Localização:"), localizacaoField, btnAtualizarMesa, btnDeletarMesa, btnRecarregarMesa); // Adicionei campos de texto aqui para atualização
        acoesMesa.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        mesaTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                capacidadeField.setText(String.valueOf(newSelection.getCapacidade()));
                localizacaoField.setText(newSelection.getLocalizacao());
            } else {
                capacidadeField.clear();
                localizacaoField.clear();
            }
        });

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(formGrid, mesaTable, acoesMesa); // Corrigi a ordem para exibir ações
        return layout;
    }

    // Aba de Itens (mantida, com chamadas DAO com conexão)
    private VBox createItemTabContent() {
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(20));

        TextField nomeItemField = new TextField();
        nomeItemField.setPromptText("Nome do Item (Ex: Risoto de Camarão)");
        TextField tipoItemField = new TextField();
        tipoItemField.setPromptText("Tipo (Ex: prato, bebida, sobremesa)");
        TextField precoItemField = new TextField();
        precoItemField.setPromptText("Preço (Ex: 45.00)");

        Button btnInserirItem = new Button("Inserir Item");
        btnInserirItem.setOnAction(e -> {
            Connection conn = null;
            try {
                conn = ConnectionFactory.getConnection();
                conn.setAutoCommit(false);
                try {
                    String nome = nomeItemField.getText();
                    String tipo = tipoItemField.getText();
                    double preco = Double.parseDouble(precoItemField.getText());

                    if (nome.isEmpty() || tipo.isEmpty() || preco <= 0) {
                        showAlert("Erro de Validação", "Preencha todos os campos e um preço válido (> 0).", Alert.AlertType.WARNING);
                        return;
                    }

                    Item novoItem = new Item(nome, tipo, preco);
                    int idGerado = itemDao.inserir(novoItem, conn);

                    if (idGerado > 0) {
                        conn.commit();
                        showAlert("Sucesso", "Item inserido com ID: " + idGerado, Alert.AlertType.INFORMATION);
                        nomeItemField.clear();
                        tipoItemField.clear();
                        precoItemField.clear();
                        carregarTabelaItens();
                        carregarItensDisponiveis();
                    } else {
                        conn.rollback();
                        showAlert("Erro", "Falha ao inserir item.", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException nfe) {
                    conn.rollback();
                    showAlert("Erro de Entrada", "Preço deve ser um número válido.", Alert.AlertType.ERROR);
                } catch (SQLException ex) {
                    conn.rollback();
                    showAlert("Erro no Banco de Dados", "Erro ao inserir item: " + ex.getMessage(), Alert.AlertType.ERROR);
                } catch (Exception ex) {
                    conn.rollback();
                    showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            } catch (SQLException ex) {
                showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
            } finally {
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (SQLException ex) {
                    showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        formGrid.add(new Label("Nome:"), 0, 0);
        formGrid.add(nomeItemField, 1, 0);
        formGrid.add(new Label("Tipo:"), 0, 1);
        formGrid.add(tipoItemField, 1, 1);
        formGrid.add(new Label("Preço:"), 0, 2);
        formGrid.add(precoItemField, 1, 2);
        GridPane.setColumnSpan(btnInserirItem, 2);
        formGrid.add(btnInserirItem, 0, 3);


        itemTable = new TableView<>();
        TableColumn<Item, Number> colItemId = new TableColumn<>("ID");
        colItemId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdItem()));
        colItemId.setPrefWidth(50);

        TableColumn<Item, String> colItemNome = new TableColumn<>("Nome");
        colItemNome.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNome()));
        colItemNome.setPrefWidth(200);

        TableColumn<Item, String> colItemTipo = new TableColumn<>("Tipo");
        colItemTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipo()));
        colItemTipo.setPrefWidth(120);

        TableColumn<Item, Number> colItemPreco = new TableColumn<>("Preço");
        colItemPreco.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPreco()));
        colItemPreco.setPrefWidth(100);

        itemTable.getColumns().addAll(colItemId, colItemNome, colItemTipo, colItemPreco);
        itemTable.setItems(itens);


        Button btnAtualizarItem = new Button("Atualizar Item");
        btnAtualizarItem.setOnAction(e -> {
            Item itemSelecionado = itemTable.getSelectionModel().getSelectedItem();
            if (itemSelecionado != null) {
                Connection conn = null;
                try {
                    conn = ConnectionFactory.getConnection();
                    conn.setAutoCommit(false);
                    try {
                        String novoNome = nomeItemField.getText();
                        String novoTipo = tipoItemField.getText();
                        double novoPreco = Double.parseDouble(precoItemField.getText());

                        if (novoNome.isEmpty() || novoTipo.isEmpty() || novoPreco <= 0) {
                            showAlert("Erro de Validação", "Preencha todos os campos e um preço válido (> 0).", Alert.AlertType.WARNING);
                            return;
                        }

                        itemSelecionado.setNome(novoNome);
                        itemSelecionado.setTipo(novoTipo);
                        itemSelecionado.setPreco(novoPreco);
                        itemDao.atualizar(itemSelecionado, conn);
                        conn.commit();
                        showAlert("Sucesso", "Item atualizado com sucesso!", Alert.AlertType.INFORMATION);
                        nomeItemField.clear();
                        tipoItemField.clear();
                        precoItemField.clear();
                        carregarTabelaItens();
                        carregarItensDisponiveis();
                    } catch (NumberFormatException nfe) {
                        conn.rollback();
                        showAlert("Erro de Entrada", "Preço deve ser um número válido.", Alert.AlertType.ERROR);
                    } catch (SQLException ex) {
                        conn.rollback();
                        showAlert("Erro no Banco de Dados", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                    } catch (Exception ex) {
                        conn.rollback();
                        showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                } catch (SQLException ex) {
                    showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
                } finally {
                    try {
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            conn.close();
                        }
                    } catch (SQLException ex) {
                        showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            } else {
                showAlert("Atenção", "Selecione um item na tabela para atualizar.", Alert.AlertType.WARNING);
            }
        });

        Button btnDeletarItem = new Button("Deletar Item");
        btnDeletarItem.setOnAction(e -> {
            Item itemSelecionado = itemTable.getSelectionModel().getSelectedItem();
            if (itemSelecionado != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja deletar o Item ID: " + itemSelecionado.getIdItem() + " (" + itemSelecionado.getNome() + ")?", ButtonType.YES, ButtonType.NO);
                confirmAlert.setHeaderText("Confirmar Deleção");
                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        Connection conn = null;
                        try {
                            conn = ConnectionFactory.getConnection();
                            conn.setAutoCommit(false);
                            try {
                                itemDao.deletar(itemSelecionado.getIdItem(), conn);
                                conn.commit();
                                showAlert("Sucesso", "Item deletado com sucesso!", Alert.AlertType.INFORMATION);
                                carregarTabelaItens();
                                carregarItensDisponiveis();
                            } catch (SQLException ex) {
                                conn.rollback();
                                showAlert("Erro no Banco de Dados", "Erro ao deletar item: " + ex.getMessage(), Alert.AlertType.ERROR);
                            } catch (Exception ex) {
                                conn.rollback();
                                showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        } catch (SQLException ex) {
                            showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
                        } finally {
                            try {
                                if (conn != null) {
                                    conn.setAutoCommit(true);
                                    conn.close();
                                }
                            } catch (SQLException ex) {
                                showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        }
                    }
                });
            } else {
                showAlert("Atenção", "Selecione um item para deletar.", Alert.AlertType.WARNING);
            }
        });

        Button btnRecarregarItem = new Button("Recarregar Itens");
        btnRecarregarItem.setOnAction(e -> carregarTabelaItens());

        HBox acoesItem = new HBox(15);
        acoesItem.setPadding(new Insets(10, 20, 10, 20));
        acoesItem.getChildren().addAll(btnAtualizarItem, btnDeletarItem, btnRecarregarItem);
        acoesItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nomeItemField.setText(newSelection.getNome());
                tipoItemField.setText(newSelection.getTipo());
                precoItemField.setText(String.valueOf(newSelection.getPreco()));
            } else {
                nomeItemField.clear();
                tipoItemField.clear();
                precoItemField.clear();
            }
        });

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(formGrid, itemTable, acoesItem);
        return layout;
    }

    // Aba de Reservas (mantida, com chamadas DAO com conexão)
    private VBox createReservaTabContent() {
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(20));

        TextField nomeResponsavelField = new TextField();
        nomeResponsavelField.setPromptText("Nome do Responsável");
        TextField numeroPessoasField = new TextField();
        numeroPessoasField.setPromptText("Número de Pessoas");
        ComboBox<Mesa> mesaComboBox = new ComboBox<>();
        mesaComboBox.setItems(mesasDisponiveis);
        mesaComboBox.setPromptText("Selecione a Mesa");
        mesaComboBox.setCellFactory(lv -> new ListCell<Mesa>() {
            @Override
            protected void updateItem(Mesa item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : "ID " + item.getIdMesa() + " (" + item.getCapacidade() + " pessoas - " + item.getLocalizacao() + ")");
            }
        });
        mesaComboBox.setButtonCell(new ListCell<Mesa>() {
            @Override
            protected void updateItem(Mesa item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Selecione a Mesa" : "ID " + item.getIdMesa() + " (" + item.getCapacidade() + " pessoas - " + item.getLocalizacao() + ")");
            }
        });

        DatePicker dataReservaPicker = new DatePicker();
        dataReservaPicker.setPromptText("Data da Reserva");
        TextField horaReservaField = new TextField();
        horaReservaField.setPromptText("Hora (HH:MM) - Ex: 19:30");
        TextField observacaoField = new TextField();
        observacaoField.setPromptText("Observação (opcional)");

        Button btnInserirReserva = new Button("Inserir Reserva");
        btnInserirReserva.setOnAction(e -> {
            Connection conn = null;
            try {
                conn = ConnectionFactory.getConnection();
                conn.setAutoCommit(false);
                try {
                    String nomeResponsavel = nomeResponsavelField.getText();
                    int numeroPessoas = Integer.parseInt(numeroPessoasField.getText());
                    Mesa mesaSelecionada = mesaComboBox.getSelectionModel().getSelectedItem();
                    LocalDate dataReserva = dataReservaPicker.getValue();
                    LocalTime horaReserva = LocalTime.parse(horaReservaField.getText());
                    String observacao = observacaoField.getText();

                    if (nomeResponsavel.isEmpty() || numeroPessoas <= 0 || mesaSelecionada == null || dataReserva == null || horaReservaField.getText().isEmpty()) {
                        showAlert("Erro de Validação", "Preencha todos os campos obrigatórios.", Alert.AlertType.WARNING);
                        return;
                    }
                    if (numeroPessoas > mesaSelecionada.getCapacidade()) {
                        showAlert("Erro de Validação", "Número de pessoas excede a capacidade da mesa selecionada.", Alert.AlertType.WARNING);
                        return;
                    }

                    if (reservaDao.verificarConflito(mesaSelecionada.getIdMesa(), dataReserva, horaReserva, 0, conn)) { // Passa a conexão
                        showAlert("Conflito de Horário", "A mesa selecionada já possui uma reserva para este horário.", Alert.AlertType.ERROR);
                        return;
                    }

                    Reserva novaReserva = new Reserva(nomeResponsavel, numeroPessoas, mesaSelecionada.getIdMesa(), dataReserva, horaReserva, observacao);
                    int idGerado = reservaDao.inserir(novaReserva, conn); // Passa a conexão

                    if (idGerado > 0) {
                        conn.commit();
                        showAlert("Sucesso", "Reserva inserida com ID: " + idGerado, Alert.AlertType.INFORMATION);
                        nomeResponsavelField.clear();
                        numeroPessoasField.clear();
                        mesaComboBox.getSelectionModel().clearSelection();
                        dataReservaPicker.setValue(null);
                        horaReservaField.clear();
                        observacaoField.clear();
                        carregarTabelaReservas();
                    } else {
                        conn.rollback();
                        showAlert("Erro", "Falha ao inserir reserva.", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException nfe) {
                    conn.rollback();
                    showAlert("Erro de Entrada", "Número de pessoas deve ser um número inteiro.", Alert.AlertType.ERROR);
                } catch (DateTimeParseException dtpe) {
                    conn.rollback();
                    showAlert("Erro de Formato", "Formato de hora inválido. Use HH:MM (Ex: 19:30).", Alert.AlertType.ERROR);
                } catch (SQLException ex) {
                    conn.rollback();
                    showAlert("Erro no Banco de Dados", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                } catch (Exception ex) {
                    conn.rollback();
                    showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            } catch (SQLException ex) {
                showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
            } finally {
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (SQLException ex) {
                    showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        formGrid.add(new Label("Responsável:"), 0, 0);
        formGrid.add(nomeResponsavelField, 1, 0);
        formGrid.add(new Label("Pessoas:"), 0, 1);
        formGrid.add(numeroPessoasField, 1, 1);
        formGrid.add(new Label("Mesa:"), 0, 2);
        formGrid.add(mesaComboBox, 1, 2);
        formGrid.add(new Label("Data:"), 0, 3);
        formGrid.add(dataReservaPicker, 1, 3);
        formGrid.add(new Label("Hora:"), 0, 4);
        formGrid.add(horaReservaField, 1, 4);
        formGrid.add(new Label("Obs:"), 0, 5);
        formGrid.add(observacaoField, 1, 5);
        GridPane.setColumnSpan(btnInserirReserva, 2);
        formGrid.add(btnInserirReserva, 0, 6);


        reservaTable = new TableView<>();
        TableColumn<Reserva, Number> colReservaId = new TableColumn<>("ID");
        colReservaId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdReserva()));
        colReservaId.setPrefWidth(50);

        TableColumn<Reserva, String> colReservaResponsavel = new TableColumn<>("Responsável");
        colReservaResponsavel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomeResponsavel()));
        colReservaResponsavel.setPrefWidth(150);

        TableColumn<Reserva, Number> colReservaPessoas = new TableColumn<>("Pessoas");
        colReservaPessoas.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNumeroPessoas()));
        colReservaPessoas.setPrefWidth(70);

        TableColumn<Reserva, Number> colReservaMesa = new TableColumn<>("Mesa ID");
        colReservaMesa.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdMesa()));
        colReservaMesa.setPrefWidth(70);

        TableColumn<Reserva, String> colReservaData = new TableColumn<>("Data");
        colReservaData.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDataReserva().toString()));
        colReservaData.setPrefWidth(100);

        TableColumn<Reserva, String> colReservaHora = new TableColumn<>("Hora");
        colReservaHora.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHoraReserva().toString()));
        colReservaHora.setPrefWidth(80);

        TableColumn<Reserva, String> colReservaObs = new TableColumn<>("Observação");
        colReservaObs.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getObservacao()));
        colReservaObs.setPrefWidth(200);

        reservaTable.getColumns().addAll(colReservaId, colReservaResponsavel, colReservaPessoas, colReservaMesa, colReservaData, colReservaHora, colReservaObs);
        reservaTable.setItems(reservas);


        Button btnAtualizarReserva = new Button("Atualizar Reserva");
        btnAtualizarReserva.setOnAction(e -> {
            Reserva reservaSelecionada = reservaTable.getSelectionModel().getSelectedItem();
            if (reservaSelecionada != null) {
                Connection conn = null;
                try {
                    conn = ConnectionFactory.getConnection();
                    conn.setAutoCommit(false);
                    try {
                        String novoNomeResponsavel = nomeResponsavelField.getText();
                        int novoNumeroPessoas = Integer.parseInt(numeroPessoasField.getText());
                        Mesa novaMesaSelecionada = mesaComboBox.getSelectionModel().getSelectedItem();
                        LocalDate novaDataReserva = dataReservaPicker.getValue();
                        LocalTime novaHoraReserva = LocalTime.parse(horaReservaField.getText());
                        String novaObservacao = observacaoField.getText();

                        if (novoNomeResponsavel.isEmpty() || novoNumeroPessoas <= 0 || novaMesaSelecionada == null || novaDataReserva == null || horaReservaField.getText().isEmpty()) {
                            showAlert("Erro de Validação", "Preencha todos os campos obrigatórios para atualização.", Alert.AlertType.WARNING);
                            return;
                        }
                        if (novoNumeroPessoas > novaMesaSelecionada.getCapacidade()) {
                            showAlert("Erro de Validação", "Número de pessoas excede a capacidade da nova mesa selecionada.", Alert.AlertType.WARNING);
                            return;
                        }

                        if (reservaDao.verificarConflito(novaMesaSelecionada.getIdMesa(), novaDataReserva, novaHoraReserva, reservaSelecionada.getIdReserva(), conn)) {
                            showAlert("Conflito de Horário", "A mesa selecionada já possui uma reserva para este novo horário.", Alert.AlertType.ERROR);
                            return;
                        }

                        reservaSelecionada.setNomeResponsavel(novoNomeResponsavel);
                        reservaSelecionada.setNumeroPessoas(novoNumeroPessoas);
                        reservaSelecionada.setIdMesa(novaMesaSelecionada.getIdMesa());
                        reservaSelecionada.setDataReserva(novaDataReserva);
                        reservaSelecionada.setHoraReserva(novaHoraReserva);
                        reservaSelecionada.setObservacao(novaObservacao);
                        reservaDao.atualizar(reservaSelecionada, conn);
                        conn.commit();
                        showAlert("Sucesso", "Reserva atualizada com sucesso!", Alert.AlertType.INFORMATION);
                        nomeResponsavelField.clear();
                        numeroPessoasField.clear();
                        mesaComboBox.getSelectionModel().clearSelection();
                        dataReservaPicker.setValue(null);
                        horaReservaField.clear();
                        observacaoField.clear();
                        carregarTabelaReservas();
                    } catch (NumberFormatException nfe) {
                        conn.rollback();
                        showAlert("Erro de Entrada", "Número de pessoas deve ser um número inteiro.", Alert.AlertType.ERROR);
                    } catch (DateTimeParseException dtpe) {
                        conn.rollback();
                        showAlert("Erro de Formato", "Formato de hora inválido. Use HH:MM (Ex: 19:30).", Alert.AlertType.ERROR);
                    } catch (SQLException ex) {
                        conn.rollback();
                        showAlert("Erro no Banco de Dados", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                    } catch (Exception ex) {
                        conn.rollback();
                        showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                } catch (SQLException ex) {
                    showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
                } finally {
                    try {
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            conn.close();
                        }
                    } catch (SQLException ex) {
                        showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            } else {
                showAlert("Atenção", "Selecione uma reserva na tabela para atualizar.", Alert.AlertType.WARNING);
            }
        });

        Button btnDeletarReserva = new Button("Deletar Reserva");
        btnDeletarReserva.setOnAction(e -> {
            Reserva reservaSelecionada = reservaTable.getSelectionModel().getSelectedItem();
            if (reservaSelecionada != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja deletar a Reserva ID: " + reservaSelecionada.getIdReserva() + " (" + reservaSelecionada.getNomeResponsavel() + ")?", ButtonType.YES, ButtonType.NO);
                confirmAlert.setHeaderText("Confirmar Deleção");
                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        Connection conn = null;
                        try {
                            conn = ConnectionFactory.getConnection();
                            conn.setAutoCommit(false);
                            try {
                                reservaDao.deletar(reservaSelecionada.getIdReserva(), conn);
                                conn.commit();
                                showAlert("Sucesso", "Reserva deletada com sucesso!", Alert.AlertType.INFORMATION);
                                carregarTabelaReservas();
                            } catch (SQLException ex) {
                                conn.rollback();
                                showAlert("Erro no Banco de Dados", "Erro ao deletar reserva: " + ex.getMessage(), Alert.AlertType.ERROR);
                            } catch (Exception ex) {
                                conn.rollback();
                                showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        } catch (SQLException ex) {
                            showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
                        } finally {
                            try {
                                if (conn != null) {
                                    conn.setAutoCommit(true);
                                    conn.close();
                                }
                            } catch (SQLException ex) {
                                showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        }
                    }
                });
            } else {
                showAlert("Atenção", "Selecione uma reserva para deletar.", Alert.AlertType.WARNING);
            }
        });

        Button btnRecarregarReserva = new Button("Recarregar Reservas");
        btnRecarregarReserva.setOnAction(e -> carregarTabelaReservas());

        HBox acoesReserva = new HBox(15);
        acoesReserva.setPadding(new Insets(10, 20, 10, 20));
        acoesReserva.getChildren().addAll(btnAtualizarReserva, btnDeletarReserva, btnRecarregarReserva);
        acoesReserva.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        reservaTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nomeResponsavelField.setText(newSelection.getNomeResponsavel());
                numeroPessoasField.setText(String.valueOf(newSelection.getNumeroPessoas()));
                mesaComboBox.getSelectionModel().select(getMesaById(newSelection.getIdMesa()));
                dataReservaPicker.setValue(newSelection.getDataReserva());
                horaReservaField.setText(newSelection.getHoraReserva().toString());
                observacaoField.setText(newSelection.getObservacao());
            } else {
                nomeResponsavelField.clear();
                numeroPessoasField.clear();
                mesaComboBox.getSelectionModel().clearSelection();
                dataReservaPicker.setValue(null);
                horaReservaField.clear();
                observacaoField.clear();
            }
        });


        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(formGrid, reservaTable, acoesReserva);
        return layout;
    }

    // Aba de Pedidos (Ajustada com validações de pago e filtro)
    private VBox createPedidoTabContent() {
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(20));

        // Campos do Pedido
        ComboBox<Mesa> mesaPedidoComboBox = new ComboBox<>();
        mesaPedidoComboBox.setItems(mesasDisponiveis);
        mesaPedidoComboBox.setPromptText("Selecione a Mesa");
        mesaPedidoComboBox.setCellFactory(lv -> new ListCell<Mesa>() {
            @Override
            protected void updateItem(Mesa item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : "ID " + item.getIdMesa() + " (" + item.getCapacidade() + " pessoas - " + item.getLocalizacao() + ")");
            }
        });
        mesaPedidoComboBox.setButtonCell(new ListCell<Mesa>() {
            @Override
            protected void updateItem(Mesa item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Selecione a Mesa" : "ID " + item.getIdMesa() + " (" + item.getCapacidade() + " pessoas - " + item.getLocalizacao() + ")");
            }
        });

        ComboBox<Funcionario> garcomComboBox = new ComboBox<>();
        garcomComboBox.setItems(garconsDisponiveis);
        garcomComboBox.setPromptText("Selecione o Garçom");
        garcomComboBox.setCellFactory(lv -> new ListCell<Funcionario>() {
            @Override
            protected void updateItem(Funcionario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : "ID " + item.getId() + " - " + item.getNome());
            }
        });
        garcomComboBox.setButtonCell(new ListCell<Funcionario>() {
            @Override
            protected void updateItem(Funcionario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Selecione o Garçom" : "ID " + item.getId() + " - " + item.getNome());
            }
        });

        ComboBox<Funcionario> gerenteComboBox = new ComboBox<>();
        gerenteComboBox.setItems(gerentesDisponiveis);
        gerenteComboBox.setPromptText("Selecione o Gerente (opcional)");
        gerenteComboBox.setCellFactory(lv -> new ListCell<Funcionario>() {
            @Override
            protected void updateItem(Funcionario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : "ID " + item.getId() + " - " + item.getNome());
            }
        });
        gerenteComboBox.setButtonCell(new ListCell<Funcionario>() {
            @Override
            protected void updateItem(Funcionario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Selecione o Gerente" : "ID " + item.getId() + " - " + item.getNome());
            }
        });

        CheckBox entregueCheckBox = new CheckBox("Pedido Entregue");
        TextField descontoField = new TextField();
        descontoField.setPromptText("Desconto (%) - opcional");

        // --- Seção para adicionar itens ao pedido atual ---
        ComboBox<Item> itemAdicionarComboBox = new ComboBox<>();
        itemAdicionarComboBox.setItems(itensDisponiveis);
        itemAdicionarComboBox.setPromptText("Adicionar Item");
        itemAdicionarComboBox.setCellFactory(lv -> new ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getNome() + " (R$" + String.format("%.2f", item.getPreco()) + ")");
            }
        });
        itemAdicionarComboBox.setButtonCell(new ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Adicionar Item" : item.getNome() + " (R$" + String.format("%.2f", item.getPreco()) + ")");
            }
        });

        TextField quantidadeItemField = new TextField();
        quantidadeItemField.setPromptText("Qtd");
        quantidadeItemField.setPrefWidth(50);

        Button btnAdicionarItem = new Button("Adicionar");
        btnAdicionarItem.setOnAction(e -> {
            Item itemSelecionado = itemAdicionarComboBox.getSelectionModel().getSelectedItem();
            if (itemSelecionado != null && !quantidadeItemField.getText().isEmpty()) {
                try {
                    int quantidade = Integer.parseInt(quantidadeItemField.getText());
                    if (quantidade <= 0) {
                        showAlert("Erro", "Quantidade deve ser maior que zero.", Alert.AlertType.WARNING);
                        return;
                    }
                    itensPedidoAtual.add(new PedidoItem(
                            itemSelecionado.getIdItem(),
                            itemSelecionado.getNome(),
                            itemSelecionado.getTipo(),
                            quantidade,
                            itemSelecionado.getPreco()
                    ));
                    quantidadeItemField.clear();
                    itemAdicionarComboBox.getSelectionModel().clearSelection();
                } catch (NumberFormatException nfe) {
                    showAlert("Erro", "Quantidade deve ser um número inteiro.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Atenção", "Selecione um item e a quantidade.", Alert.AlertType.WARNING);
            }
        });

        HBox addItemBox = new HBox(10, new Label("Item:"), itemAdicionarComboBox, new Label("Qtd:"), quantidadeItemField, btnAdicionarItem);
        addItemBox.setPadding(new Insets(10, 0, 10, 0));

        itensPedidoTable = new TableView<>();
        TableColumn<PedidoItem, String> colNomeItem = new TableColumn<>("Item");
        colNomeItem.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomeItem()));
        colNomeItem.setPrefWidth(150);

        TableColumn<PedidoItem, Number> colQtdItem = new TableColumn<>("Qtd");
        colQtdItem.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantidade()));
        colQtdItem.setPrefWidth(50);

        TableColumn<PedidoItem, Number> colPrecoUnitItem = new TableColumn<>("Preço Unit.");
        colPrecoUnitItem.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrecoUnitario()));
        colPrecoUnitItem.setPrefWidth(100);

        TableColumn<PedidoItem, Number> colSubtotalItem = new TableColumn<>("Subtotal");
        colSubtotalItem.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getSubtotal()));
        colSubtotalItem.setPrefWidth(100);

        itensPedidoTable.getColumns().addAll(colNomeItem, colQtdItem, colPrecoUnitItem, colSubtotalItem);
        itensPedidoTable.setItems(itensPedidoAtual);

        Button btnRemoverItem = new Button("Remover Item Selecionado");
        btnRemoverItem.setOnAction(e -> {
            PedidoItem itemRemover = itensPedidoTable.getSelectionModel().getSelectedItem();
            if (itemRemover != null) {
                itensPedidoAtual.remove(itemRemover);
            } else {
                showAlert("Atenção", "Selecione um item na lista de itens do pedido para remover.", Alert.AlertType.WARNING);
            }
        });
        HBox itemActionsBox = new HBox(10, btnRemoverItem);
        itemActionsBox.setPadding(new Insets(0, 0, 10, 0));


        // --- Botões de Ação do Pedido Principal ---
        Button btnInserirPedido = new Button("Criar Pedido");
        btnInserirPedido.setOnAction(e -> {
            Connection conn = null;
            try {
                conn = ConnectionFactory.getConnection();
                conn.setAutoCommit(false);

                Mesa mesaSelecionada = mesaPedidoComboBox.getSelectionModel().getSelectedItem();
                Funcionario garcomSelecionado = garcomComboBox.getSelectionModel().getSelectedItem();
                Funcionario gerenteSelecionado = gerenteComboBox.getSelectionModel().getSelectedItem();

                Integer idGarcom = (garcomSelecionado != null) ? garcomSelecionado.getId() : null;
                Integer idGerente = (gerenteSelecionado != null) ? gerenteSelecionado.getId() : null;
                int idMesa = (mesaSelecionada != null) ? mesaSelecionada.getIdMesa() : 0;

                Double desconto = null;
                if (!descontoField.getText().isEmpty()) {
                    desconto = Double.parseDouble(descontoField.getText());
                    if (desconto < 0 || desconto > 100) {
                        showAlert("Erro de Validação", "Desconto deve ser entre 0 e 100%.", Alert.AlertType.WARNING);
                        conn.rollback();
                        return;
                    }
                }

                if (desconto != null && desconto > 0) {
                    if (gerenteSelecionado == null) {
                        showAlert("Erro de Validação", "Desconto exige a seleção de um gerente para autorização.", Alert.AlertType.ERROR);
                        conn.rollback();
                        return;
                    }
                    if (desconto > 15.00) {
                        showAlert("Erro de Validação", "Desconto excede o limite máximo permitido pelo gerente (15%).", Alert.AlertType.ERROR);
                        conn.rollback();
                        return;
                    }
                }

                if (mesaSelecionada == null || idGarcom == null || itensPedidoAtual.isEmpty()) {
                    showAlert("Erro de Validação", "Mesa, Garçom e ao menos um item são obrigatórios para o pedido.", Alert.AlertType.WARNING);
                    conn.rollback();
                    return;
                }

                Pedido novoPedido = new Pedido(idGarcom, idGerente, idMesa, entregueCheckBox.isSelected(), desconto);
                novoPedido.setItensDoPedido(new ArrayList<>(itensPedidoAtual));

                int idGerado = pedidoDao.inserir(novoPedido, conn);

                if (idGerado > 0) {
                    conn.commit();
                    showAlert("Sucesso", "Pedido criado com ID: " + idGerado, Alert.AlertType.INFORMATION);
                    mesaPedidoComboBox.getSelectionModel().clearSelection();
                    garcomComboBox.getSelectionModel().clearSelection();
                    gerenteComboBox.getSelectionModel().clearSelection();
                    entregueCheckBox.setSelected(false);
                    descontoField.clear();
                    itensPedidoAtual.clear();
                    carregarTabelaPedidos();
                    carregarPedidosNaoPagos();
                } else {
                    conn.rollback();
                    showAlert("Erro", "Falha ao criar pedido.", Alert.AlertType.ERROR);
                }

            } catch (NumberFormatException nfe) {
                try { if (conn != null) conn.rollback(); } catch (SQLException ex) { /* ignored */ }
                showAlert("Erro de Entrada", "Desconto e quantidade devem ser números válidos.", Alert.AlertType.ERROR);
            } catch (SQLException ex) {
                try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* ignored */ }
                showAlert("Erro no Banco de Dados", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                ex.printStackTrace();
            } catch (Exception ex) {
                try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* ignored */ }
                showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                ex.printStackTrace();
            } finally {
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (SQLException ex) {
                    showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        Button btnAtualizarPedido = new Button("Atualizar Pedido Selecionado");
        btnAtualizarPedido.setOnAction(e -> {
            Pedido pedidoSelecionado = pedidoTable.getSelectionModel().getSelectedItem();
            if (pedidoSelecionado != null) {
                if (pedidoSelecionado.isPago()) { // VALIDAÇÃO: Impedir atualização se o pedido já está pago
                    showAlert("Pedido Pago", "Este pedido já foi pago e não pode ser alterado.", Alert.AlertType.WARNING);
                    return;
                }

                Connection conn = null;
                try {
                    conn = ConnectionFactory.getConnection();
                    conn.setAutoCommit(false);
                    try {
                        Mesa mesaSelecionada = mesaPedidoComboBox.getSelectionModel().getSelectedItem();
                        Funcionario garcomSelecionado = garcomComboBox.getSelectionModel().getSelectedItem();
                        Funcionario gerenteSelecionado = gerenteComboBox.getSelectionModel().getSelectedItem();

                        Integer idGarcom = (garcomSelecionado != null) ? garcomSelecionado.getId() : null;
                        Integer idGerente = (gerenteSelecionado != null) ? gerenteSelecionado.getId() : null;
                        int idMesa = (mesaSelecionada != null) ? mesaSelecionada.getIdMesa() : 0;

                        Double desconto = null;
                        if (!descontoField.getText().isEmpty()) {
                            desconto = Double.parseDouble(descontoField.getText());
                            if (desconto < 0 || desconto > 100) {
                                showAlert("Erro de Validação", "Desconto deve ser entre 0 e 100%.", Alert.AlertType.WARNING);
                                conn.rollback();
                                return;
                            }
                        }

                        if (desconto != null && desconto > 0) {
                            if (gerenteSelecionado == null) {
                                showAlert("Erro de Validação", "Desconto exige a seleção de um gerente para autorização.", Alert.AlertType.ERROR);
                                conn.rollback();
                                return;
                            }
                            if (desconto > 15.00) {
                                showAlert("Erro de Validação", "Desconto excede o limite máximo permitido pelo gerente (15%).", Alert.AlertType.ERROR);
                                conn.rollback();
                                return;
                            }
                        }

                        if (mesaSelecionada == null || idGarcom == null || itensPedidoAtual.isEmpty()) {
                            showAlert("Erro de Validação", "Mesa, Garçom e ao menos um item são obrigatórios para o pedido.", Alert.AlertType.WARNING);
                            conn.rollback();
                            return;
                        }

                        pedidoSelecionado.setIdMesa(idMesa);
                        pedidoSelecionado.setIdGarcom(idGarcom);
                        pedidoSelecionado.setIdGerente(idGerente);
                        pedidoSelecionado.setEntregue(entregueCheckBox.isSelected());
                        pedidoSelecionado.setDesconto(desconto);
                        pedidoSelecionado.setItensDoPedido(new ArrayList<>(itensPedidoAtual));

                        pedidoDao.atualizar(pedidoSelecionado, conn);
                        conn.commit();
                        showAlert("Sucesso", "Pedido ID: " + pedidoSelecionado.getIdPedido() + " atualizado com sucesso!", Alert.AlertType.INFORMATION);

                        mesaPedidoComboBox.getSelectionModel().clearSelection();
                        garcomComboBox.getSelectionModel().clearSelection();
                        gerenteComboBox.getSelectionModel().clearSelection();
                        entregueCheckBox.setSelected(false);
                        descontoField.clear();
                        itensPedidoAtual.clear();
                        carregarTabelaPedidos();
                        carregarPedidosNaoPagos();
                    } catch (NumberFormatException nfe) {
                        try { if (conn != null) conn.rollback(); } catch (SQLException ex) { /* ignored */ }
                        showAlert("Erro de Entrada", "Desconto e quantidade devem ser números válidos.", Alert.AlertType.ERROR);
                    } catch (SQLException ex) {
                        try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* ignored */ }
                        showAlert("Erro no Banco de Dados", "Ocorreu um erro ao atualizar o pedido: " + ex.getMessage(), Alert.AlertType.ERROR);
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* ignored */ }
                        showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                        ex.printStackTrace();
                    }
                } catch (SQLException ex) {
                    showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
                } finally {
                    try {
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            conn.close();
                        }
                    } catch (SQLException ex) {
                        showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            } else {
                showAlert("Atenção", "Selecione um pedido na tabela para atualizar.", Alert.AlertType.WARNING);
            }
        });

        Button btnDeletarPedido = new Button("Deletar Pedido Selecionado");
        btnDeletarPedido.setOnAction(e -> {
            Pedido pedidoSelecionado = pedidoTable.getSelectionModel().getSelectedItem();
            if (pedidoSelecionado != null) {
                if (pedidoSelecionado.isPago()) { // VALIDAÇÃO: Impedir deleção se o pedido já está pago
                    showAlert("Pedido Pago", "Este pedido já foi pago e não pode ser deletado.", Alert.AlertType.WARNING);
                    return;
                }

                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja deletar o Pedido ID: " + pedidoSelecionado.getIdPedido() + "?", ButtonType.YES, ButtonType.NO);
                confirmAlert.setHeaderText("Confirmar Deleção");
                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        Connection conn = null;
                        try {
                            conn = ConnectionFactory.getConnection();
                            conn.setAutoCommit(false);
                            try {
                                pedidoDao.deletar(pedidoSelecionado.getIdPedido(), conn);
                                conn.commit();
                                showAlert("Sucesso", "Pedido deletado com sucesso!", Alert.AlertType.INFORMATION);
                                carregarTabelaPedidos();
                                carregarPedidosNaoPagos();
                            } catch (SQLException ex) {
                                conn.rollback();
                                showAlert("Erro no Banco de Dados", "Erro ao deletar pedido: " + ex.getMessage(), Alert.AlertType.ERROR);
                            } catch (Exception ex) {
                                conn.rollback();
                                showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        } catch (SQLException ex) {
                            showAlert("Erro de Conexão", "Não foi possível estabelecer conexão com o banco de dados: " + ex.getMessage(), Alert.AlertType.ERROR);
                        } finally {
                            try {
                                if (conn != null) {
                                    conn.setAutoCommit(true);
                                    conn.close();
                                }
                            } catch (SQLException ex) {
                                showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        }
                    }
                });
            } else {
                showAlert("Atenção", "Selecione um pedido para deletar.", Alert.AlertType.WARNING);
            }
        });

        // NOVO: CheckBox para filtrar pedidos
        mostrarPagosCheckBox = new CheckBox("Mostrar Pedidos Pagos"); // Inicializado como variável de instância
        mostrarPagosCheckBox.setOnAction(e -> carregarTabelaPedidos()); // Recarrega a tabela com base no estado do checkbox

        Button btnRecarregarPedidos = new Button("Recarregar Pedidos");
        btnRecarregarPedidos.setOnAction(e -> carregarTabelaPedidos());

        HBox pedidoActions = new HBox(15, btnInserirPedido, btnAtualizarPedido, btnDeletarPedido, mostrarPagosCheckBox, btnRecarregarPedidos);
        pedidoActions.setPadding(new Insets(10, 20, 10, 20));
        pedidoActions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Grid para o formulário principal do pedido
        formGrid.add(new Label("Mesa:"), 0, 0);
        formGrid.add(mesaPedidoComboBox, 1, 0);
        formGrid.add(new Label("Garçom:"), 0, 1);
        formGrid.add(garcomComboBox, 1, 1);
        formGrid.add(new Label("Gerente:"), 2, 0);
        formGrid.add(gerenteComboBox, 3, 0);
        formGrid.add(new Label("Entregue:"), 2, 1);
        formGrid.add(entregueCheckBox, 3, 1);
        formGrid.add(new Label("Desconto (%):"), 0, 2);
        formGrid.add(descontoField, 1, 2);

        VBox itemManagementBox = new VBox(10);
        itemManagementBox.setPadding(new Insets(10, 0, 10, 0));
        itemManagementBox.getChildren().addAll(
                new Label("Adicionar Itens ao Pedido:"),
                addItemBox,
                itensPedidoTable,
                itemActionsBox
        );
        GridPane.setColumnSpan(itemManagementBox, 4);
        formGrid.add(itemManagementBox, 0, 3);


        // Tabela de Pedidos
        pedidoTable = new TableView<>();
        TableColumn<Pedido, Number> colPedidoId = new TableColumn<>("ID");
        colPedidoId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdPedido()));
        colPedidoId.setPrefWidth(50);

        TableColumn<Pedido, Number> colPedidoMesa = new TableColumn<>("Mesa ID");
        colPedidoMesa.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdMesa()));
        colPedidoMesa.setPrefWidth(70);

        TableColumn<Pedido, String> colPedidoGarcom = new TableColumn<>("Garçom");
        colPedidoGarcom.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getIdGarcom() != null ? getFuncionarioById(data.getValue().getIdGarcom()).getNome() : "N/A"
        ));
        colPedidoGarcom.setPrefWidth(120);

        TableColumn<Pedido, String> colPedidoGerente = new TableColumn<>("Gerente");
        colPedidoGerente.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getIdGerente() != null ? getFuncionarioById(data.getValue().getIdGerente()).getNome() : "Nenhum"
        ));
        colPedidoGerente.setPrefWidth(120);

        TableColumn<Pedido, String> colPedidoDataHora = new TableColumn<>("Data/Hora");
        colPedidoDataHora.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        ));
        colPedidoDataHora.setPrefWidth(150);

        TableColumn<Pedido, String> colPedidoEntregue = new TableColumn<>("Entregue");
        colPedidoEntregue.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isEntregue() ? "Sim" : "Não"));
        colPedidoEntregue.setPrefWidth(80);

        TableColumn<Pedido, Number> colPedidoDesconto = new TableColumn<>("Desconto (%)");
        colPedidoDesconto.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getDesconto() != null ? data.getValue().getDesconto() : 0.0));
        colPedidoDesconto.setPrefWidth(100);

        TableColumn<Pedido, String> colPedidoPago = new TableColumn<>("Pago");
        colPedidoPago.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isPago() ? "Sim" : "Não"));
        colPedidoPago.setPrefWidth(60);

        TableColumn<Pedido, Number> colPedidoTotal = new TableColumn<>("Total (R$)");
        colPedidoTotal.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getValorTotalComDesconto()));
        colPedidoTotal.setPrefWidth(100);

        pedidoTable.getColumns().addAll(colPedidoId, colPedidoMesa, colPedidoGarcom, colPedidoGerente, colPedidoDataHora, colPedidoEntregue, colPedidoDesconto, colPedidoPago, colPedidoTotal);
        pedidoTable.setItems(pedidos);

        pedidoTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                mesaPedidoComboBox.getSelectionModel().select(getMesaById(newSelection.getIdMesa()));
                garcomComboBox.getSelectionModel().select(getFuncionarioById(newSelection.getIdGarcom()));
                gerenteComboBox.getSelectionModel().select(getFuncionarioById(newSelection.getIdGerente()));
                entregueCheckBox.setSelected(newSelection.isEntregue());
                descontoField.setText(newSelection.getDesconto() != null ? String.valueOf(newSelection.getDesconto()) : "");
                itensPedidoAtual.setAll(newSelection.getItensDoPedido());
            } else {
                mesaPedidoComboBox.getSelectionModel().clearSelection();
                garcomComboBox.getSelectionModel().clearSelection();
                gerenteComboBox.getSelectionModel().clearSelection();
                entregueCheckBox.setSelected(false);
                descontoField.clear();
                itensPedidoAtual.clear();
            }
        });

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(formGrid, pedidoActions, pedidoTable);
        return layout;
    }

    // --- Método para criar o conteúdo da Aba de Pagamentos ---
    private VBox createPagamentoTabContent() {
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(20));

        ComboBox<Pedido> pedidoPagamentoComboBox = new ComboBox<>();
        pedidoPagamentoComboBox.setItems(pedidosNaoPagos);
        pedidoPagamentoComboBox.setPromptText("Selecione o Pedido a Pagar");
        pedidoPagamentoComboBox.setCellFactory(lv -> new ListCell<Pedido>() {
            @Override
            protected void updateItem(Pedido item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : "ID " + item.getIdPedido() + " | Mesa " + item.getIdMesa() + " | Total: R$" + String.format("%.2f", item.getValorTotalComDesconto()));
            }
        });
        pedidoPagamentoComboBox.setButtonCell(new ListCell<Pedido>() {
            @Override
            protected void updateItem(Pedido item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Selecione o Pedido" : "ID " + item.getIdPedido() + " | Mesa " + item.getIdMesa() + " | Total: R$" + String.format("%.2f", item.getValorTotalComDesconto()));
            }
        });

        TextField valorTotalPagamentoField = new TextField();
        valorTotalPagamentoField.setPromptText("Valor Total Pago");
        valorTotalPagamentoField.setEditable(false);

        ComboBox<String> metodoPagamentoComboBox = new ComboBox<>();
        metodoPagamentoComboBox.getItems().addAll("Dinheiro", "Cartão de Crédito", "Cartão de Débito", "PIX", "Voucher");
        metodoPagamentoComboBox.setPromptText("Método de Pagamento");

        pedidoPagamentoComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                valorTotalPagamentoField.setText(String.format("%.2f", newSelection.getValorTotalComDesconto()));
            } else {
                valorTotalPagamentoField.clear();
            }
        });

        Button btnRegistrarPagamento = new Button("Registrar Pagamento");
        btnRegistrarPagamento.setOnAction(e -> {
            Connection conn = null;
            try {
                conn = ConnectionFactory.getConnection();
                conn.setAutoCommit(false);

                Pedido pedidoSelecionado = pedidoPagamentoComboBox.getSelectionModel().getSelectedItem();
                String metodoPagamento = metodoPagamentoComboBox.getValue();

                if (pedidoSelecionado == null || metodoPagamento == null || valorTotalPagamentoField.getText().isEmpty()) {
                    showAlert("Erro de Validação", "Selecione um pedido e um método de pagamento.", Alert.AlertType.WARNING);
                    conn.rollback();
                    return;
                }

                double valorTotal = Double.parseDouble(valorTotalPagamentoField.getText().replace(",", "."));

                if (pedidoSelecionado.isPago()) {
                    showAlert("Erro", "Este pedido já foi registrado como pago.", Alert.AlertType.ERROR);
                    conn.rollback();
                    return;
                }

                Pagamento novoPagamento = new Pagamento(pedidoSelecionado.getIdPedido(), valorTotal, metodoPagamento);

                int idGerado = pagamentoDao.inserir(novoPagamento, conn);

                if (idGerado > 0) {
                    conn.commit();
                    showAlert("Sucesso", "Pagamento registrado com ID: " + idGerado + ". Pedido ID " + pedidoSelecionado.getIdPedido() + " marcado como pago.", Alert.AlertType.INFORMATION);

                    pedidoPagamentoComboBox.getSelectionModel().clearSelection();
                    metodoPagamentoComboBox.getSelectionModel().clearSelection();
                    valorTotalPagamentoField.clear();

                    carregarTabelaPagamentos();
                    carregarPedidosNaoPagos();
                    carregarTabelaPedidos();
                } else {
                    conn.rollback();
                    showAlert("Erro", "Falha ao registrar pagamento.", Alert.AlertType.ERROR);
                }

            } catch (NumberFormatException nfe) {
                try { if (conn != null) conn.rollback(); } catch (SQLException ex) { /* ignored */ }
                showAlert("Erro de Entrada", "Valor do pagamento inválido.", Alert.AlertType.ERROR);
            } catch (SQLException ex) {
                try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* ignored */ }
                showAlert("Erro no Banco de Dados", "Ocorreu um erro ao registrar o pagamento: " + ex.getMessage(), Alert.AlertType.ERROR);
                ex.printStackTrace();
            } catch (Exception ex) {
                try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* ignored */ }
                showAlert("Erro Inesperado", "Ocorreu um erro: " + ex.getMessage(), Alert.AlertType.ERROR);
                ex.printStackTrace();
            } finally {
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (SQLException ex) {
                    showAlert("Erro de Conexão", "Falha ao fechar conexão: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        formGrid.add(new Label("Pedido:"), 0, 0);
        formGrid.add(pedidoPagamentoComboBox, 1, 0);
        formGrid.add(new Label("Valor Total:"), 0, 1);
        formGrid.add(valorTotalPagamentoField, 1, 1);
        formGrid.add(new Label("Método:"), 0, 2);
        formGrid.add(metodoPagamentoComboBox, 1, 2);
        GridPane.setColumnSpan(btnRegistrarPagamento, 2);
        formGrid.add(btnRegistrarPagamento, 0, 3);


        pagamentoTable = new TableView<>();
        TableColumn<Pagamento, Number> colPagamentoId = new TableColumn<>("ID Pagamento");
        colPagamentoId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdPagamento()));
        colPagamentoId.setPrefWidth(80);

        TableColumn<Pagamento, Number> colPagamentoPedidoId = new TableColumn<>("ID Pedido");
        colPagamentoPedidoId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdPedido()));
        colPagamentoPedidoId.setPrefWidth(80);

        TableColumn<Pagamento, Number> colPagamentoValor = new TableColumn<>("Valor (R$)");
        colPagamentoValor.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getValorTotal()));
        colPagamentoValor.setPrefWidth(100);

        TableColumn<Pagamento, String> colPagamentoMetodo = new TableColumn<>("Método");
        colPagamentoMetodo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMetodoPagamento()));
        colPagamentoMetodo.setPrefWidth(120);

        TableColumn<Pagamento, String> colPagamentoDataHora = new TableColumn<>("Data/Hora");
        colPagamentoDataHora.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDataPagamento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        colPagamentoDataHora.setPrefWidth(150);

        pagamentoTable.getColumns().addAll(colPagamentoId, colPagamentoPedidoId, colPagamentoValor, colPagamentoMetodo, colPagamentoDataHora);
        pagamentoTable.setItems(pagamentos);

        Button btnAtualizarPagamento = new Button("Atualizar Pagamento");
        btnAtualizarPagamento.setOnAction(e -> showAlert("Funcionalidade", "Atualização de Pagamento não implementada para este projeto.", Alert.AlertType.INFORMATION));

        Button btnDeletarPagamento = new Button("Deletar Pagamento");
        btnDeletarPagamento.setOnAction(e -> showAlert("Funcionalidade", "Deleção de Pagamento não implementada para este projeto.", Alert.AlertType.INFORMATION));

        Button btnRecarregarPagamentos = new Button("Recarregar Pagamentos");
        btnRecarregarPagamentos.setOnAction(e -> carregarTabelaPagamentos());

        HBox acoesPagamento = new HBox(15, btnAtualizarPagamento, btnDeletarPagamento, btnRecarregarPagamentos);
        acoesPagamento.setPadding(new Insets(10, 20, 10, 20));
        acoesPagamento.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(formGrid, acoesPagamento, pagamentoTable);
        return layout;
    }


    // --- Métodos Auxiliares Comuns (definidos apenas UMA VEZ na classe) ---

    private void inserirEspecializacao(int idFuncionario, String tipo, Connection conn) throws SQLException {
        String sql = null;
        switch (tipo) {
            case "Garçom" -> sql = "INSERT INTO Garcom (id_funcionario, setor_atendimento) VALUES (?, 'Varanda')";
            case "Cozinheiro" -> sql = "INSERT INTO Cozinheiro (id_funcionario, espec_cul) VALUES (?, 'Grelhados')";
            case "Bartender" -> sql = "INSERT INTO Bartender (id_funcionario, espec_bar) VALUES (?, 'Drinks')";
            case "Gerente" -> sql = "INSERT INTO Gerente (id_funcionario, nivel_acesso, limite_desconto) VALUES (?, 'Alto', 20.00)";
        }

        if (sql != null) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idFuncionario);
                stmt.executeUpdate();
            }
        }
    }

    private void carregarTabelaFuncionarios() {
        funcionarios.setAll(funcionarioDao.listarTodos());
    }

    private void carregarFuncionariosDisponiveis() {
        garconsDisponiveis.clear();
        gerentesDisponiveis.clear();
        for (Funcionario f : funcionarioDao.listarTodos()) {
            if (f.getId() == 1 || f.getId() == 2 || f.getId() == 3) {
                garconsDisponiveis.add(f);
            }
            if (f.getId() == 8) {
                gerentesDisponiveis.add(f);
            }
        }
    }

    private Funcionario getFuncionarioById(Integer id) {
        if (id == null) return null;
        try {
            return funcionarioDao.buscarPorId(id); // Usa o método que abre nova conexão
        } catch (Exception e) {
            System.err.println("Erro ao buscar funcionário por ID para exibição: " + e.getMessage());
            return null;
        }
    }

    private void carregarTabelaMesas() {
        mesas.setAll(mesaDao.listarTodos());
    }

    private void carregarMesasDisponiveis() {
        mesasDisponiveis.setAll(mesaDao.listarTodos());
    }

    private Mesa getMesaById(int id) {
        try {
            return mesaDao.buscarPorId(id); // Usa o método que abre nova conexão
        } catch (Exception e) {
            System.err.println("Erro ao buscar mesa por ID para exibição: " + e.getMessage());
            return null;
        }
    }

    private void carregarTabelaItens() {
        itens.setAll(itemDao.listarTodos());
    }

    private void carregarItensDisponiveis() {
        itensDisponiveis.setAll(itemDao.listarTodos());
    }

    private Item getItemById(int id) {
        try {
            return itemDao.buscarPorId(id); // Usa o método que abre nova conexão
        } catch (Exception e) {
            System.err.println("Erro ao buscar item por ID para exibição: " + e.getMessage());
            return null;
        }
    }

    private void carregarTabelaReservas() {
        reservas.setAll(reservaDao.listarTodos());
    }

    private void carregarTabelaPedidos() {
        if (mostrarPagosCheckBox != null && mostrarPagosCheckBox.isSelected()) {
            pedidos.setAll(pedidoDao.listarTodos());
        } else {
            pedidos.setAll(pedidoDao.listarPedidosNaoPagos());
        }
    }

    private void carregarTabelaPagamentos() {
        pagamentos.setAll(pagamentoDao.listarTodos());
    }

    private void carregarPedidosNaoPagos() {
        pedidosNaoPagos.setAll(pedidoDao.listarPedidosNaoPagos());
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}