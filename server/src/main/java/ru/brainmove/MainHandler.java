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
import ru.brainmove.file.*;
import ru.brainmove.service.UserServiceBean;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static ru.brainmove.file.FileUtils.MAX_BYTE_SIZE;

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
                    Path filePath = Paths.get(SERVER_STORAGE + userPath + fr.getFilename());
                    if (Files.exists(filePath)) {
                        if (fr.isForDeleting()) {
                            Files.delete(filePath);
                            sendFileList(ctx, userPath);
                        } else {

                            File file = filePath.toFile();
                            if (file.length() > MAX_BYTE_SIZE) {
                                long fileCounts = 1;
                                long remainFileSize = file.length();
                                long fileCountBySize = remainFileSize / MAX_BYTE_SIZE + 1;
                                byte[] bytes = new byte[MAX_BYTE_SIZE];
                                Path tempPath = Paths.get(SERVER_STORAGE + TEMP_FOLDER + userPath);
                                Files.createDirectories(tempPath);
                                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                                while (remainFileSize > 0) {
                                    if (remainFileSize > MAX_BYTE_SIZE) {
                                        randomAccessFile.read(bytes, 0, MAX_BYTE_SIZE);
                                        remainFileSize -= MAX_BYTE_SIZE;
                                    } else {
                                        bytes = new byte[(int) remainFileSize];
                                        randomAccessFile.read(bytes, 0, (int) remainFileSize);
                                        remainFileSize = 0;
                                    }
                                    String filePartName = String.format("%s.%03d", fr.getFilename(), fileCounts++);
                                    final FileMessage fm = new FileMessage(filePartName, bytes, fileCountBySize, fr.getFilename());
                                    ctx.writeAndFlush(fm);
                                }
                                randomAccessFile.close();
                            } else {
                                final FileMessage fm = new FileMessage(Paths.get(SERVER_STORAGE + userPath + fr.getFilename()));
                                ctx.writeAndFlush(fm);
                            }
                        }
                    }
                }
                if (msg instanceof FileMessage) {
                    final FileMessage fm = (FileMessage) msg;
                    final String path = SERVER_STORAGE + (fm.getFileCounts() != 1 ? TEMP_FOLDER : "") + userPath;
                    FileUtils.createFileAndDirectories(path, fm);
                    // если не 1 файл, то надо прочитать директорию
                    if (fm.getFileCounts() > 1) {
                        // файлы все сложились в темп, преобразуем их
                        final String finalPath = SERVER_STORAGE + userPath;
                        if (FileUtils.createFileFromTemp(path, finalPath, fm))
                            sendFileList(ctx, userPath);
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
