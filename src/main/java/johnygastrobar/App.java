package johnygastrobar;

import johnygastrobar.dao.FuncionarioDAO;
import johnygastrobar.model.Funcionario;
import johnygastrobar.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        FuncionarioDAO dao = new FuncionarioDAO();
        Scanner sc = new Scanner(System.in);
        int opcao;

        do {
            System.out.println("\n1 - Inserir funcionário");
            System.out.println("2 - Listar funcionários");
            System.out.println("3 - Atualizar salário");
            System.out.println("4 - Deletar funcionário");
            System.out.println("0 - Sair");
            System.out.print("Escolha: ");
            opcao = sc.nextInt();
            sc.nextLine(); // limpar buffer

            Connection conn = null; // Conexão para a transação do App
            try {
                conn = ConnectionFactory.getConnection();
                conn.setAutoCommit(false); // Inicia transação para operações do console

                switch (opcao) {
                    case 1 -> {
                        System.out.print("Nome: ");
                        String nome = sc.nextLine();
                        System.out.print("CPF: ");
                        String cpf = sc.nextLine();
                        System.out.print("Salário: ");
                        double salario = sc.nextDouble();
                        sc.nextLine();
                        dao.inserir(new Funcionario(nome, cpf, salario), conn); // <--- CORRIGIDO: Passa 'conn'
                    }
                    case 2 -> dao.listarTodos().forEach(System.out::println);
                    case 3 -> {
                        System.out.print("ID do funcionário: ");
                        int id = sc.nextInt();
                        System.out.print("Novo salário: ");
                        double sal = sc.nextDouble();
                        dao.atualizarSalario(id, sal, conn); // <--- CORRIGIDO: Passa 'conn'
                    }
                    case 4 -> {
                        System.out.print("ID do funcionário: ");
                        int id = sc.nextInt();
                        dao.deletar(id, conn); // <--- CORRIGIDO: Passa 'conn'
                    }
                }
                conn.commit(); // Confirma transação
            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback(); // Desfaz transação em caso de erro
                    } catch (SQLException ex) {
                        System.err.println("Erro ao fazer rollback no App: " + ex.getMessage());
                    }
                }
                System.err.println("Erro no App (SQL): " + e.getMessage());
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close(); // Fecha a conexão
                    } catch (SQLException e) {
                        System.err.println("Erro ao fechar conexão no App: " + e.getMessage());
                    }
                }
            }
        } while (opcao != 0);

        System.out.println("Encerrado.");
        sc.close();
    }
}