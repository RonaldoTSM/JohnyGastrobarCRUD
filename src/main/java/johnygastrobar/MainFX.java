package johnygastrobar;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class MainFX extends Application {

    private final FuncionarioDAO dao = new FuncionarioDAO();
    private final ObservableList<Funcionario> funcionarios = FXCollections.observableArrayList();
    private TableView<Funcionario> table;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Johny Gastrobar - Funcionários");

        // Formulário
        TextField nomeField = new TextField();
        TextField cpfField = new TextField();
        TextField salarioField = new TextField();
        ComboBox<String> tipoBox = new ComboBox<>();
        tipoBox.getItems().addAll("Garçom", "Cozinheiro", "Bartender", "Gerente");

        Button btnInserir = new Button("Inserir");
        btnInserir.setOnAction(e -> {
            try {
                String nome = nomeField.getText();
                String cpf = cpfField.getText();
                double salario = Double.parseDouble(salarioField.getText());
                String tipo = tipoBox.getValue();

                if (nome.isEmpty() || cpf.isEmpty() || tipo == null) {
                    showAlert("Preencha todos os campos corretamente.");
                    return;
                }

                // Inserir no Funcionario e obter ID
                int novoId = inserirFuncionario(nome, cpf, salario);

                // Inserir na tabela de especialização
                if (novoId > 0) {
                    inserirEspecializacao(novoId, tipo);
                    showAlert("Funcionário e tipo inseridos com sucesso!");
                    nomeField.clear(); cpfField.clear(); salarioField.clear(); tipoBox.getSelectionModel().clearSelection();
                    carregarTabela();
                }

            } catch (Exception ex) {
                showAlert("Erro ao inserir: " + ex.getMessage());
            }
        });

        HBox form = new HBox(10,
                new Label("Nome:"), nomeField,
                new Label("CPF:"), cpfField,
                new Label("Salário:"), salarioField,
                new Label("Tipo:"), tipoBox,
                btnInserir);
        form.setPadding(new Insets(10));

        // Tabela
        table = new TableView<>();
        TableColumn<Funcionario, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));

        TableColumn<Funcionario, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNome()));

        TableColumn<Funcionario, String> colCpf = new TableColumn<>("CPF");
        colCpf.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCpf()));

        TableColumn<Funcionario, Number> colSalario = new TableColumn<>("Salário");
        colSalario.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getSalario()));

        table.getColumns().addAll(colId, colNome, colCpf, colSalario);
        table.setItems(funcionarios);

        // Atualizar salário
        TextField novoSalarioField = new TextField();
        Button btnAtualizar = new Button("Atualizar Salário");
        btnAtualizar.setOnAction(e -> {
            Funcionario f = table.getSelectionModel().getSelectedItem();
            if (f != null && !novoSalarioField.getText().isEmpty()) {
                double novoSalario = Double.parseDouble(novoSalarioField.getText());
                dao.atualizarSalario(f.getId(), novoSalario);
                novoSalarioField.clear();
                carregarTabela();
            }
        });

        // Deletar
        Button btnDeletar = new Button("Deletar");
        btnDeletar.setOnAction(e -> {
            Funcionario f = table.getSelectionModel().getSelectedItem();
            if (f != null) {
                dao.deletar(f.getId());
                carregarTabela();
            }
        });

        // Recarregar
        Button btnRecarregar = new Button("Recarregar");
        btnRecarregar.setOnAction(e -> carregarTabela());

        HBox acoes = new HBox(10, new Label("Novo salário:"), novoSalarioField, btnAtualizar, btnDeletar, btnRecarregar);
        acoes.setPadding(new Insets(10));

        VBox layout = new VBox(10, form, table, acoes);
        Scene scene = new Scene(layout, 1000, 500);
        stage.setScene(scene);
        stage.show();

        carregarTabela();
    }

    private int inserirFuncionario(String nome, String cpf, double salario) throws SQLException {
        String sql = "INSERT INTO Funcionario (nome, cpf, salario, data_contratacao) VALUES (?, ?, ?, CURDATE())";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nome);
            stmt.setString(2, cpf);
            stmt.setDouble(3, salario);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("ID não gerado.");
            }
        }
    }

    private void inserirEspecializacao(int idFuncionario, String tipo) throws SQLException {
        String sql = null;
        switch (tipo) {
            case "Garçom" -> sql = "INSERT INTO Garcom (id_funcionario, setor_atendimento) VALUES (?, 'Varanda')";
            case "Cozinheiro" -> sql = "INSERT INTO Cozinheiro (id_funcionario, espec_cul) VALUES (?, 'Grelhados')";
            case "Bartender" -> sql = "INSERT INTO Bartender (id_funcionario, espec_bar) VALUES (?, 'Drinks')";
            case "Gerente" -> sql = "INSERT INTO Gerente (id_funcionario, nivel_acesso, limite_desconto) VALUES (?, 'Alto', 20.00)";
        }

        if (sql != null) {
            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idFuncionario);
                stmt.executeUpdate();
            }
        }
    }

    private void carregarTabela() {
        funcionarios.setAll(dao.listarTodos());
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
