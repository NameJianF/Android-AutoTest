package live.itrip.client.common;

import javafx.scene.control.ButtonType;

/**
 * Created by IntelliJ IDEA.
 * Description: modal 窗口返回值
 *
 * @author JianF
 * Date:  2017/11/20
 * Time:  16:25
 * Modify:
 */
public class ModalReturnValue {
    private ButtonType buttonType;
    private Object value;

    public ModalReturnValue() {
        // 设置默认值:关闭窗口
        this.buttonType = ButtonType.CLOSE;
    }



    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ButtonType getButtonType() {
        return buttonType;
    }

    public void setButtonType(ButtonType buttonType) {
        this.buttonType = buttonType;
    }
}
