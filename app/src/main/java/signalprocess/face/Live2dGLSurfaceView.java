package signalprocess.face;

/**
 * Created by Administrator on 2017/5/11.
 */

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.android.UtOpenGL;
import jp.live2d.framework.L2DEyeBlink;
import jp.live2d.framework.L2DStandardID;
import vtalk.activity.TalkActivity;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import java.io.InputStream;

public class Live2dGLSurfaceView extends GLSurfaceView {

    private String TAG = "Live2D";

    Live2dRenderer mLive2dRenderer;

    //private L2DMotionManager mMotionManager;

    private Context mContext;

    public Live2dGLSurfaceView(Context context) {
        super(context);
        this.mContext = context;
    }
    public Live2dGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void init(TalkActivity activity, String MODEL_PATH, String[] TEXTURE_PATHS,
                     float wRatio, float hRatio) {

        this.mLive2dRenderer = new Live2dRenderer();
        this.mLive2dRenderer.setUpModel(activity, MODEL_PATH, TEXTURE_PATHS, wRatio, hRatio);
        this.setRenderer(this.mLive2dRenderer);
    }
}

class Live2dRenderer implements GLSurfaceView.Renderer {
    private TalkActivity mActivity;
    private Live2DModelAndroid	live2DModel;
    private L2DEyeBlink mEyeBlink;

    private String MODEL_PATH;
    private String[] TEXTURE_PATHS;

    private float wRatio, hRatio;

    public void setUpModel(TalkActivity activity, String MODEL_PATH, String[] TEXTURE_PATHS,
                           float wRatio, float hRatio) {
        this.mActivity = activity;
        this.MODEL_PATH = MODEL_PATH;
        this.TEXTURE_PATHS = TEXTURE_PATHS;
        this.wRatio = wRatio;
        this.hRatio = hRatio;

        this.mEyeBlink = new L2DEyeBlink();
    }

    private void loadLive2dModel(GL10 gl, String modelPath, String[] texturePath) {
        try {
            InputStream in = this.mActivity.getAssets().open(modelPath);
            live2DModel = Live2DModelAndroid.loadModel(in);
            in.close();

            for (int i = 0; i < texturePath.length; i++) {
                InputStream tin = this.mActivity.getAssets().open(texturePath[i]);
                int texNo = UtOpenGL.loadTexture(gl, tin, true);
                live2DModel.setTexture(i, texNo);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        mEyeBlink.updateParam(live2DModel);

        double[] emotion = mActivity.getEmotion();

        live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_Z, (float) emotion[0], 0.75f);
        live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_X , (float) emotion[1], 0.75f);
        live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_Y , (float) emotion[2], 0.75f);
        live2DModel.setParamFloat(L2DStandardID.PARAM_MOUTH_OPEN_Y, (float) emotion[3], 0.75f);
        live2DModel.setParamFloat(L2DStandardID.PARAM_MOUTH_FORM, (float) emotion[3], 0.75f);

        live2DModel.setGL(gl);
        live2DModel.update();
        live2DModel.draw();
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        gl.glViewport(0 , 0 , width , height);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        float modelWidth = live2DModel.getCanvasWidth();
        float aspect = (float)width/height;

        gl.glOrthof(0, wRatio*modelWidth, hRatio*modelWidth / aspect, 0, 0.5f, -0.5f);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d("Render", "onSurfaceCreated");
        loadLive2dModel(gl, MODEL_PATH, TEXTURE_PATHS);
    }
}
