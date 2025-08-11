import java.util.*;
import java.io.*;

// Stock class representing a stock
class Stock {
    String symbol;
    String name;
    double price;

    public Stock(String symbol, String name, double price) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
    }

    public void updatePrice() {
        double change = (Math.random() * 10) - 5; // random change between -5 and +5
        price += change;
        if (price < 1) price = 1; // minimum price
    }

    public String toString() {
        return symbol + " (" + name + ") - $" + String.format("%.2f", price);
    }
}

// Transaction class representing a buy/sell operation
class Transaction {
    String type;
    Stock stock;
    int quantity;
    double total;

    public Transaction(String type, Stock stock, int quantity, double total) {
        this.type = type;
        this.stock = stock;
        this.quantity = quantity;
        this.total = total;
    }

    public String toString() {
        return type + " " + quantity + " shares of " + stock.symbol + " at $" +
               String.format("%.2f", stock.price) + " | Total: $" + String.format("%.2f", total);
    }
}

// Portfolio class
class Portfolio {
    Map<String, Integer> holdings = new HashMap<>();
    List<Transaction> history = new ArrayList<>();
    double balance;

    public Portfolio(double startingBalance) {
        this.balance = startingBalance;
    }

    public void buyStock(Stock stock, int quantity) {
        double cost = stock.price * quantity;
        if (cost > balance) {
            System.out.println("Insufficient balance to buy.");
            return;
        }
        balance -= cost;
        holdings.put(stock.symbol, holdings.getOrDefault(stock.symbol, 0) + quantity);
        history.add(new Transaction("BUY", stock, quantity, cost));
        System.out.println("Bought " + quantity + " shares of " + stock.symbol);
    }

    public void sellStock(Stock stock, int quantity) {
        int owned = holdings.getOrDefault(stock.symbol, 0);
        if (quantity > owned) {
            System.out.println("You don't own enough shares to sell.");
            return;
        }
        double proceeds = stock.price * quantity;
        balance += proceeds;
        if (quantity == owned) {
            holdings.remove(stock.symbol);
        } else {
            holdings.put(stock.symbol, owned - quantity);
        }
        history.add(new Transaction("SELL", stock, quantity, proceeds));
        System.out.println("Sold " + quantity + " shares of " + stock.symbol);
    }

    public void showPortfolio(List<Stock> marketStocks) {
        System.out.println("\n=== Portfolio ===");
        System.out.printf("Balance: $%.2f%n", balance);
        double totalValue = balance;

        if (holdings.isEmpty()) {
            System.out.println("No holdings.");
        } else {
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                String symbol = entry.getKey();
                int qty = entry.getValue();
                Stock stock = marketStocks.stream()
                        .filter(s -> s.symbol.equals(symbol))
                        .findFirst().orElse(null);
                if (stock != null) {
                    double value = stock.price * qty;
                    System.out.printf("%s: %d shares @ $%.2f = $%.2f%n", symbol, qty, stock.price, value);
                    totalValue += value;
                }
            }
        }

        System.out.printf("Total Portfolio Value: $%.2f%n", totalValue);
    }

    public void showTransactionHistory() {
        System.out.println("\n=== Transaction History ===");
        if (history.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            for (Transaction t : history) {
                System.out.println(t);
            }
        }
    }

    // Optional: Save portfolio to file
    public void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(balance);
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
            System.out.println("Portfolio saved to " + filename);
        } catch (IOException e) {
            System.out.println("Error saving portfolio.");
        }
    }

    // Optional: Load portfolio from file
    public void loadFromFile(String filename) {
        try (Scanner fileScanner = new Scanner(new File(filename))) {
            balance = Double.parseDouble(fileScanner.nextLine());
            holdings.clear();
            while (fileScanner.hasNextLine()) {
                String[] parts = fileScanner.nextLine().split(",");
                holdings.put(parts[0], Integer.parseInt(parts[1]));
            }
            System.out.println("Portfolio loaded from " + filename);
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading portfolio.");
        }
    }
}

// Main app
public class StockTradingPlatform {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Simulate a few stocks
        List<Stock> market = new ArrayList<>();
        market.add(new Stock("AAPL", "Apple Inc.", 150.00));
        market.add(new Stock("GOOGL", "Alphabet Inc.", 2800.00));
        market.add(new Stock("TSLA", "Tesla Inc.", 700.00));
        market.add(new Stock("AMZN", "Amazon.com Inc.", 3300.00));

        Portfolio portfolio = new Portfolio(10000); // Starting balance

        while (true) {
            System.out.println("\n=== Stock Trading Platform ===");
            System.out.println("1. View Market Data");
            System.out.println("2. Buy Stock");
            System.out.println("3. Sell Stock");
            System.out.println("4. View Portfolio");
            System.out.println("5. View Transaction History");
            System.out.println("6. Save Portfolio");
            System.out.println("7. Load Portfolio");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            // Update market prices every loop
            for (Stock s : market) {
                s.updatePrice();
            }

            switch (choice) {
                case "1":
                    System.out.println("\n=== Market Data ===");
                    for (Stock s : market) {
                        System.out.println(s);
                    }
                    break;
                case "2":
                    System.out.print("Enter stock symbol to buy: ");
                    String buySymbol = scanner.nextLine().toUpperCase();
                    Stock buyStock = findStockBySymbol(market, buySymbol);
                    if (buyStock == null) {
                        System.out.println("Stock not found.");
                        break;
                    }
                    System.out.print("Enter quantity: ");
                    int buyQty = Integer.parseInt(scanner.nextLine());
                    portfolio.buyStock(buyStock, buyQty);
                    break;
                case "3":
                    System.out.print("Enter stock symbol to sell: ");
                    String sellSymbol = scanner.nextLine().toUpperCase();
                    Stock sellStock = findStockBySymbol(market, sellSymbol);
                    if (sellStock == null) {
                        System.out.println("Stock not found.");
                        break;
                    }
                    System.out.print("Enter quantity: ");
                    int sellQty = Integer.parseInt(scanner.nextLine());
                    portfolio.sellStock(sellStock, sellQty);
                    break;
                case "4":
                    portfolio.showPortfolio(market);
                    break;
                case "5":
                    portfolio.showTransactionHistory();
                    break;
                case "6":
                    portfolio.saveToFile("portfolio.txt");
                    break;
                case "7":
                    portfolio.loadFromFile("portfolio.txt");
                    break;
                case "0":
                    System.out.println("Exiting... Thank you!");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static Stock findStockBySymbol(List<Stock> market, String symbol) {
        for (Stock s : market) {
            if (s.symbol.equalsIgnoreCase(symbol)) {
                return s;
            }
        }
        return null;
    }
}
