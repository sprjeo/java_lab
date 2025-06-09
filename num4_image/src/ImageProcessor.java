import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImageProcessor {
    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png"};
    private static final AtomicBoolean cancellationRequested = new AtomicBoolean(false);
    private static ExecutorService executorService;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Недостаточно аргументов");
            printUsage();
            return;
        }

        // Парсинг аргументов
        String sourceDir = args[0];
        boolean recursive = false;
        String operation = null;
        String operationParam = null;
        int paramIndex = -1;

        for (int i = 1; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "/sub":
                    recursive = true;
                    break;
                case "/s":
                case "/n":
                case "/r":
                case "/c":
                    if (operation != null) {
                        System.out.println("Может быть указана только одна операция");
                        printUsage();
                        return;
                    }
                    operation = args[i].toLowerCase();
                    paramIndex = i + 1;
                    break;
            }
        }

        if (operation == null) {
            System.out.println("Не указана операция");
            printUsage();
            return;
        }

        if (operation.equals("/s") || operation.equals("/c")) {
            if (paramIndex >= args.length) {
                System.out.println("Не указан обязательный параметр для операции " + operation);
                printUsage();
                return;
            }
            operationParam = args[paramIndex];
        }

        int processors = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(processors);

        try {
            processImages(sourceDir, recursive, operation, operationParam);
        } catch (InterruptedException e) {
            System.out.println("Обработка прервана");
        } finally {
            executorService.shutdown();
        }
    }

    private static void printUsage() {
        System.out.println("Использование:");
        System.out.println("java ImageProcessor <исходный_каталог> [/sub] [/s <коэффициент> | /n | /r | /c <целевой_каталог>]");
        System.out.println("  /sub - рекурсивный обход подкаталогов");
        System.out.println("  /s - растянуть изображение (требуется коэффициент)");
        System.out.println("  /n - создать негатив изображения");
        System.out.println("  /r - удалить изображение");
        System.out.println("  /c - скопировать изображение (требуется целевой каталог)");
    }

    private static void processImages(String sourceDir, boolean recursive, String operation, String operationParam)
            throws InterruptedException {
        Thread cancellationThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (!cancellationRequested.get()) {
                    if (System.in.available() > 0 && scanner.nextLine().equalsIgnoreCase("esc")) {
                        cancellationRequested.set(true);
                        executorService.shutdownNow();
                        System.out.println("Запрошена отмена операции...");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        cancellationThread.setDaemon(true);
        cancellationThread.start();

        List<File> imageFiles = new ArrayList<>();
        collectImageFiles(new File(sourceDir), recursive, imageFiles);

        if (imageFiles.isEmpty()) {
            System.out.println("Изображения не найдены");
            return;
        }

        System.out.println("Найдено изображений: " + imageFiles.size());
        System.out.println("Нажмите ESC для отмены операции...");

        List<Future<?>> futures = new ArrayList<>();
        for (File file : imageFiles) {
            if (cancellationRequested.get()) break;

            futures.add(executorService.submit(() -> {
                try {
                    processFile(file, operation, operationParam);
                } catch (IOException e) {
                    System.err.println("Ошибка обработки файла " + file.getPath() + ": " + e.getMessage());
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                if (!cancellationRequested.get()) {
                    System.err.println("Ошибка при выполнении задачи: " + e.getMessage());
                }
            }
        }

        if (cancellationRequested.get()) {
            System.out.println("Операция отменена пользователем");
        } else {
            System.out.println("Обработка завершена");
        }
    }

    private static void collectImageFiles(File dir, boolean recursive, List<File> imageFiles) {
        if (cancellationRequested.get()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (cancellationRequested.get()) return;

            if (file.isDirectory() && recursive) {
                collectImageFiles(file, true, imageFiles);
            } else if (isImageFile(file)) {
                imageFiles.add(file);
            }
        }
    }

    private static boolean isImageFile(File file) {
        if (!file.isFile()) return false;

        String name = file.getName().toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private static void processFile(File file, String operation, String operationParam) throws IOException {
        switch (operation) {
            case "/s":
                double scaleFactor = Double.parseDouble(operationParam);
                scaleImage(file, scaleFactor);
                break;
            case "/n":
                createNegativeImage(file);
                break;
            case "/r":
                deleteImage(file);
                break;
            case "/c":
                copyImage(file, operationParam);
                break;
        }
    }

    private static void scaleImage(File file, double scaleFactor) throws IOException {
        if (scaleFactor <= 0) {
            throw new IllegalArgumentException("Коэффициент масштабирования должен быть положительным");
        }

        BufferedImage originalImage = ImageIO.read(file);
        if (originalImage == null) {
            throw new IOException("Не удалось прочитать изображение");
        }

        int newWidth = (int) (originalImage.getWidth() * scaleFactor);
        int newHeight = (int) (originalImage.getHeight() * scaleFactor);

        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        String formatName = getFormatName(file.getName());
        ImageIO.write(scaledImage, formatName, file);
        System.out.println("Изображение масштабировано: " + file.getPath());
    }

    private static void createNegativeImage(File file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file);
        if (originalImage == null) {
            throw new IOException("Не удалось прочитать изображение");
        }

        BufferedImage negativeImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                originalImage.getType());

        for (int y = 0; y < originalImage.getHeight(); y++) {
            for (int x = 0; x < originalImage.getWidth(); x++) {
                int rgb = originalImage.getRGB(x, y);
                int r = 255 - ((rgb >> 16) & 0xFF);
                int g = 255 - ((rgb >> 8) & 0xFF);
                int b = 255 - (rgb & 0xFF);
                int newRgb = (r << 16) | (g << 8) | b;
                negativeImage.setRGB(x, y, newRgb);
            }
        }

        String formatName = getFormatName(file.getName());
        ImageIO.write(negativeImage, formatName, file);
        System.out.println("Создан негатив: " + file.getPath());
    }

    private static void deleteImage(File file) throws IOException {
        if (!file.delete()) {
            throw new IOException("Не удалось удалить файл");
        }
        System.out.println("Файл удален: " + file.getPath());
    }

    private static void copyImage(File file, String targetDir) throws IOException {
        Path targetPath = Paths.get(targetDir);
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
        }

        Path sourcePath = file.toPath();
        Path destinationPath = targetPath.resolve(file.getName());
        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Файл скопирован: " + sourcePath + " -> " + destinationPath);
    }

    private static String getFormatName(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "jpg";
        if (lower.endsWith(".png")) return "png";
        return "jpg";
    }
}