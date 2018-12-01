package ru.brainmove;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.brainmove.api.UserService;
import ru.brainmove.auth.AuthMessage;
import ru.brainmove.auth.AuthRequest;
import ru.brainmove.auth.AuthType;
import ru.brainmove.entity.Token;
import ru.brainmove.entity.User;
import ru.brainmove.file.FileListMessage;
import ru.brainmove.file.FileListRequest;
import ru.brainmove.file.FileMessage;
import ru.brainmove.file.FileRequest;
import ru.brainmove.service.UserServiceBean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private static final String SERVER_STORAGE = "server_storage/";
    private static final String TEMP_FOLDER = "temp/";
    private UserService userService;

    MainHandler() {
        this.userService = new UserServiceBean();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof AuthRequest) {
                final AuthRequest authRequest = (AuthRequest) msg;
                boolean success;
                String errMsg = "";
                if (authRequest.getAuthType() == AuthType.LOGIN) {
                    success = userService.check(authRequest.getLogin(), authRequest.getPassword());
                    if (!success) {
                        errMsg = "Неверный логин или пароль!";
                    }
                } else {
                    success = userService.registry(authRequest.getLogin(), authRequest.getPassword());
                    if (!success) {
                        errMsg = "Возникла ошибка при регистрации! Такой логин уже существует!";
                    }
                }
                final User user = (success ? userService.findByUser(authRequest.getLogin()) : null);
                final Token token = userService.createToken(user);
                final AuthMessage authMessage = new AuthMessage(success, errMsg, authRequest.getAuthType(), user, token);
                ctx.writeAndFlush(authMessage);
            } else if (msg instanceof AbstractMessage) {
                // если не authrequest, то надо проверить access token и id
                AbstractMessage abstractMessage = (AbstractMessage) msg;
                String userPath = abstractMessage.getId() + "/";
                // не прошли проверку
                if (!userService.checkToken(abstractMessage.getId(), abstractMessage.getAccessToken()))
                    return;
                if (msg instanceof FileRequest) {
                    final FileRequest fr = (FileRequest) msg;
                    if (Files.exists(Paths.get(SERVER_STORAGE + userPath + fr.getFilename()))) {
                        final FileMessage fm = new FileMessage(Paths.get(SERVER_STORAGE + userPath + fr.getFilename()));
                        ctx.writeAndFlush(fm);
                    }
                }
                if (msg instanceof FileMessage) {
                    final FileMessage fm = (FileMessage) msg;
                    final String path = SERVER_STORAGE + (fm.getFileCounts() != 1 ? TEMP_FOLDER : "") + userPath;
                    final Path filePath = Paths.get(path + fm.getFilename());
                    final Path dirPath = Paths.get(path);
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                    }
                    Files.createDirectories(dirPath);
                    Files.write(filePath, fm.getData(), StandardOpenOption.CREATE);
                    // если не 1 файл, то надо прочитать директорию
                    if (fm.getFileCounts() > 1) {
                        // файлы все сложились в темп, преобразуем их
                        long fileCounts = Files.list(Paths.get(path)).map(p -> p.getFileName().toString()).filter(p -> p.startsWith(fm.getRealFilename())).count();
                        if (fm.getFileCounts().equals(fileCounts)) {
                            final Path newFilePath = Paths.get(SERVER_STORAGE + userPath + fm.getRealFilename());
                            final Path newDirPath = Paths.get(SERVER_STORAGE + userPath);
                            if (Files.exists(newFilePath))
                                Files.delete(newFilePath);
                            Files.createDirectories(newDirPath);
                            Files.list(Paths.get(path)).filter(p -> p.getFileName().toString().startsWith(fm.getRealFilename())).forEachOrdered(p -> {
                                try {
                                    Files.write(newFilePath, Files.readAllBytes(p), (!Files.exists(newFilePath) ? StandardOpenOption.CREATE : StandardOpenOption.APPEND));
                                    Files.delete(p);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            sendFileList(ctx, userPath);
                        }
                    } else
                        sendFileList(ctx, userPath);
                }
                if (msg instanceof FileListRequest) {
                    sendFileList(ctx, userPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    private void sendFileList(ChannelHandlerContext ctx, String userPath) throws IOException {
        List<String> filesList = new ArrayList<>();
        Files.list(Paths.get(SERVER_STORAGE + userPath)).map(p -> p.getFileName().toString()).forEach(filesList::add);
        ctx.writeAndFlush(new FileListMessage(filesList));
    }
}
