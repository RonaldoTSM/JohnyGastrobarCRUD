package johnygastrobar;

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

            switch (opcao) {
                case 1 -> {
                    System.out.print("Nome: ");
                    String nome = sc.nextLine();
                    System.out.print("CPF: ");
                    String cpf = sc.nextLine();
                    System.out.print("Salário: ");
                    double salario = sc.nextDouble();
                    sc.nextLine(); // limpar buffer
                    dao.inserir(new Funcionario(nome, cpf, salario));
                }
                case 2 -> dao.listarTodos().forEach(System.out::println);
                case 3 -> {
                    System.out.print("ID do funcionário: ");
                    int id = sc.nextInt();
                    System.out.print("Novo salário: ");
                    double sal = sc.nextDouble();
                    dao.atualizarSalario(id, sal);
                }
                case 4 -> {
                    System.out.print("ID do funcionário: ");
                    int id = sc.nextInt();
                    dao.deletar(id);
                }
            }
        } while (opcao != 0);

        System.out.println("Encerrado.");
        sc.close();
    }
}
