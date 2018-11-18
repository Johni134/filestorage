package ru.brainmove;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.brainmove.api.UserService;
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
    private UserService userService;

    MainHandler() {
        this.userService = new UserServiceBean();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof FileRequest) {
                final FileRequest fr = (FileRequest) msg;
                if (Files.exists(Paths.get(SERVER_STORAGE + fr.getFilename()))) {
                    final FileMessage fm = new FileMessage(Paths.get(SERVER_STORAGE + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }
            }
            if (msg instanceof FileMessage) {
                final FileMessage fm = (FileMessage) msg;
                final Path filePath = Paths.get(SERVER_STORAGE + fm.getFilename());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
                Files.write(Paths.get(SERVER_STORAGE + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                sendFileList(ctx);
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
                final AuthMessage authMessage = new AuthMessage(success, errMsg, authRequest.getAuthType(), (success ? userService.findByUser(authRequest.getLogin()) : null));
                ctx.writeAndFlush(authMessage);
            }
            if (msg instanceof FileListRequest) {
                sendFileList(ctx);
            }
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

    private void sendFileList(ChannelHandlerContext ctx) throws IOException {
        List<String> filesList = new ArrayList<>();
        Files.list(Paths.get(SERVER_STORAGE)).map(p -> p.getFileName().toString()).forEach(filesList::add);
        ctx.writeAndFlush(new FileListMessage(filesList));
    }
}
