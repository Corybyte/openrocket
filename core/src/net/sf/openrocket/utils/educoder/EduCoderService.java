package net.sf.openrocket.utils.educoder;

import net.sf.openrocket.utils.educoder.BodyTubeCgRequest;
import net.sf.openrocket.utils.educoder.BodyTubeCpRequest;
import net.sf.openrocket.utils.educoder.BodyTubeMOIRequest;
import net.sf.openrocket.utils.educoder.DataResult;
import net.sf.openrocket.utils.educoder.DemoRequest;
import net.sf.openrocket.utils.educoder.FinSetCgRequest;
import net.sf.openrocket.utils.educoder.FinSetCpRequest;
import net.sf.openrocket.utils.educoder.FinSetMOIRequest;
import net.sf.openrocket.utils.educoder.InnerComponentCgRequest;
import net.sf.openrocket.utils.educoder.InnerComponentMOIRequest;
import net.sf.openrocket.utils.educoder.InnerTubeCgRequest;
import net.sf.openrocket.utils.educoder.InnerTubeMOIRequest;
import net.sf.openrocket.utils.educoder.LaunchLugCgRequest;
import net.sf.openrocket.utils.educoder.LaunchLugCpRequest;
import net.sf.openrocket.utils.educoder.LaunchLugMOIRequest;
import net.sf.openrocket.utils.educoder.MassComponentCgRequest;
import net.sf.openrocket.utils.educoder.MassComponentMOIRequest;
import net.sf.openrocket.utils.educoder.NoseConeCgRequest;
import net.sf.openrocket.utils.educoder.NoseConeCpRequest;
import net.sf.openrocket.utils.educoder.NoseConeMOIRequest;
import net.sf.openrocket.utils.educoder.ParachuteCgRequest;
import net.sf.openrocket.utils.educoder.ParachuteMOIRequest;
import net.sf.openrocket.utils.educoder.PodsCgRequest;
import net.sf.openrocket.utils.educoder.PodsCpRequest;
import net.sf.openrocket.utils.educoder.PodsMOIRequest;
import net.sf.openrocket.utils.educoder.RailButtonCgRequest;
import net.sf.openrocket.utils.educoder.RailButtonCpRequest;
import net.sf.openrocket.utils.educoder.RailButtonMOIRequest;
import net.sf.openrocket.utils.educoder.Result;
import net.sf.openrocket.utils.educoder.Result2;
import net.sf.openrocket.utils.educoder.ShockCordCgRequest;
import net.sf.openrocket.utils.educoder.ShockCordMOIRequest;
import net.sf.openrocket.utils.educoder.StageCgRequest;
import net.sf.openrocket.utils.educoder.StageCpRequest;
import net.sf.openrocket.utils.educoder.StageMOIRequest;
import net.sf.openrocket.utils.educoder.StreamerCgRequest;
import net.sf.openrocket.utils.educoder.StreamerMOIRequest;
import net.sf.openrocket.utils.educoder.TransitionCgRequest;
import net.sf.openrocket.utils.educoder.TransitionCpRequest;
import net.sf.openrocket.utils.educoder.TransitionMOIRequest;
import net.sf.openrocket.utils.educoder.TubeFinSetCpRequest;
import net.sf.openrocket.utils.educoder.TubeFinSetMOIRequest;
import net.sf.openrocket.utils.educoder.TubeFinsetCGRequest;
import net.sf.openrocket.utils.educoder.WholeCgRequest;
import net.sf.openrocket.utils.educoder.WholeCpRequest;
import net.sf.openrocket.utils.educoder.WholeMOIRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface EduCoderService {
    /**
     * 计算头锥组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("/Projectile/checkJson")
    Call<net.sf.openrocket.utils.educoder.Result> checkJSON (@Body Object object);

    @POST("/Projectile/checkJson2")
    Call<net.sf.openrocket.utils.educoder.Result> checkJSON2 (@Body Object object);
//位置坐标信息
    @POST("/Projectile/position")
    Call<net.sf.openrocket.utils.educoder.Result> checkJSON3 (@Body Object object);


    @POST("/Projectile/checkJSON4")
    Call<net.sf.openrocket.utils.educoder.Result> checkJSON4 (@Body Object object);
    @POST("NoseCone/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body NoseConeCgRequest request);

    @POST("Demo/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateDemo(@Body DemoRequest request);



    /***
     * 计算头锥组件转动惯量
     * @param request request
     * @return result
     */
    @POST("NoseCone/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body NoseConeMOIRequest request);

    /**
     * 计算头锥组件压心位置
     *
     * @param request request
     * @return result
     */
    @POST("NoseCone/calculateCP")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCP(@Body NoseConeCpRequest request);


    /**
     * 计算箭体组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("BodyTube/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body BodyTubeCgRequest request);

    /**
     * 计算箭体组件压心位置
     *
     * @param request request
     * @return result
     */
    @POST("BodyTube/calculateCP")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCP(@Body BodyTubeCpRequest request);

    /**
     * 计算箭体组件转动惯量
     *
     * @param request request
     * @return result
     */
    @POST("BodyTube/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body BodyTubeMOIRequest request);

    /**
     * 计算尾翼组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("FinSet/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body FinSetCgRequest request);

    /**
     * 计算尾翼组件压心位置
     *
     * @param request request
     * @return result
     */
    @POST("FinSet/calculateCP")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCP(@Body FinSetCpRequest request);

    /**
     * 计算尾翼组件转动惯量
     *
     * @param request request
     * @return result
     */
    @POST("FinSet/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body FinSetMOIRequest request);

    /**
     * 计算级间段组件压心位置
     *
     * @param request request
     * @return result
     */
    @POST("Transition/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body TransitionCgRequest request);

    @POST("Transition/calculateCP")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCP(@Body TransitionCpRequest request);

    @POST("Transition/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body TransitionMOIRequest request);


    /**
     * 计算管状翼组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("TubeFinSet/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body TubeFinsetCGRequest request);

    /**
     * 计算管状翼组件压心位置
     *
     * @param request request
     * @return result
     */
    @POST("TubeFinSet/calculateCP")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCP(@Body TubeFinSetCpRequest request);

    /**
     * 计算管状翼组件转动惯量
     *
     * @param request request
     * @return result
     */
    @POST("TubeFinSet/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body TubeFinSetMOIRequest request);

    /**
     * 计算发射套柄组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("LaunchLug/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body LaunchLugCgRequest request);


    /**
     * 计算发射套柄组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("LaunchLug/calculateCP")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCP(@Body LaunchLugCpRequest request);


    /**
     * 计算发射套柄组件转动惯量
     *
     * @param request request
     * @return result
     */
    @POST("LaunchLug/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body LaunchLugMOIRequest request);


    /**
     * 计算RailButton组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("RailButton/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body RailButtonCgRequest request);

    /**
     * 计算RailButton组件压心位置
     *
     * @param request request
     * @return result
     */
    @POST("RailButton/calculateCP")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCP(@Body RailButtonCpRequest request);


    /**
     * 计算RailButton组件转动惯量
     *
     * @param request request
     * @return result
     */
    @POST("RailButton/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body RailButtonMOIRequest request);


    /**
     * 计算内筒组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("InnerTube/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body InnerTubeCgRequest request);


    /**
     * 计算连接器、中心环、隔板、发动机组件转动惯量
     *
     * @param request request
     * @return result
     */
    @POST("InnerTube/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body InnerTubeMOIRequest request);


    /**
     * 计算连接器、中心环、隔板、发动机组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("InnerComponent/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body InnerComponentCgRequest request);


    /**
     * 计算连接器、中心环、隔板、发动机组件转动惯量
     *
     * @param request request
     * @return result
     */
    @POST("InnerComponent/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body InnerComponentMOIRequest request);


    /**
     * 计算Stage组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("Stage/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body StageCgRequest request);


    /**
     * 计算Stage组件压心位置
     *
     * @param request request
     * @return result
     */
    @POST("Stage/calculateCP")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCP(@Body StageCpRequest request);


    /**
     * 计算Stage组件转动惯量
     *
     * @param request request
     * @return result
     */
    @POST("Stage/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body StageMOIRequest request);


    /**
     * 计算Pods组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("Pods/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body PodsCgRequest request);


    /**
     * 计算Pods组件压心位置
     *
     * @param request request
     * @return result
     */
    @POST("Pods/calculateCP")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCP(@Body PodsCpRequest request);


    /**
     * 计算Pods组件转动惯量
     *
     * @param request request
     * @return result
     */
    @POST("Pods/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body PodsMOIRequest request);


    /**
     * 计算降落伞组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("Parachute/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body ParachuteCgRequest request);


    /**
     * 计算降落伞组件转动惯量
     *
     * @param request request
     * @return result
     */
    @POST("Parachute/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body ParachuteMOIRequest request);


    /**
     * 计算飘带组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("Streamer/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body StreamerCgRequest request);


    /**
     * 计算飘带组件转动惯量
     *
     * @param request request
     * @return result
     */
    @POST("Streamer/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body StreamerMOIRequest request);


    /**
     * 计算减震索组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("ShockCord/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body ShockCordCgRequest request);


    /**
     * 计算减震索组件转动惯量
     *
     * @param request request
     * @return result
     */
    @POST("ShockCord/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body ShockCordMOIRequest request);


    /**
     * 计算MassComponent组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("MassComponent/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body MassComponentCgRequest request);


    /**
     * 计算MassComponent组件转动惯量
     *
     * @param request request
     * @return result
     */
    @POST("MassComponent/calculateMOI")
    Call<net.sf.openrocket.utils.educoder.Result2> calculateMOI(@Body MassComponentMOIRequest request);


    /**
     * 计算总体组件重心位置
     *
     * @param request request
     * @return result
     */
    @POST("Whole/calculateCG")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCG(@Body WholeCgRequest request);

    /**
     * 计算总体组件压心位置
     *
     * @param request request
     * @return result
     */
    @POST("Whole/calculateCP")
    Call<Result> calculateCP(@Body WholeCpRequest request);


    /**
     * 计算总体组件转动惯量位置
     *
     * @param request request
     * @return result
     */
    @POST("Whole/calculateMOI")
    Call<Result2> calculateMOI(@Body WholeMOIRequest request);

    /**
     * 数据点拟合推力曲线
     *
     * @return result
     */
    @POST("Motor/point")
    Call<net.sf.openrocket.utils.educoder.DataResult> calculatePoint(@Body Integer status);

    /**
     * 函数拟合推力曲线
     *
     * @return result
     */
    @POST("Motor/function")
    Call<net.sf.openrocket.utils.educoder.DataResult> calculateFunction(@Body Integer status);

    /**
     * 弹体法向力系数
     */
    @POST("Projectile/calculateCN")
    Call<net.sf.openrocket.utils.educoder.Result> calculateCN(@Body HullCNRequest status);

    /**
     * 弹体压差阻力
     */
    @POST("Projectile/calculatePressureCD")
    Call<net.sf.openrocket.utils.educoder.Result> calculatePressureCD(@Body BodyPressureCDRequest status);


    @POST("Whole/cd")
    Call<Result> calculateCD(@Body TotalBasalResistanceRequest status);


    /**
     * 弹体压差阻力
     */
    @POST("Projectile/calculateFinsetPressureCD")
    Call<net.sf.openrocket.utils.educoder.Result> calculateFinsetPressureCD(@Body FinsetPressureCDRequest status);



    /**
     * 轴向力系数
     */
    @POST("Projectile/calculateAxialCD")
    Call<net.sf.openrocket.utils.educoder.Result> calculateAxialCD(@Body AxialCDRequest status);





    /**
     * 摩擦力系数
     */
    @POST("Projectile/calculateFrictionCD")
    Call<net.sf.openrocket.utils.educoder.Result> calculateFrictionCD(@Body FrictionCDRequest status);



    /**
     *轨迹
     */
    @POST("Projectile/Acceleration")
    Call<net.sf.openrocket.utils.educoder.Result> Acceleration(@Body AccelerationRequest status);


    @POST("Projectile/Stability")
    Call<Result> calculateStability(@Body StabilityRequest stabilityRequest);

    @POST("/Wing/calculateCN")
    Call<Result> Wing_calculateCN(@Body WingCNRequest wingCNRequest);

    @POST("/calculateComponentNonAxialForces")
    Call<Result> calculateComponentNonAxialForces(@Body componentForcesRequest componentForcesRequest);



    @POST("Projectile/totalMoment")
    Call<Result> calculateTotalMoment(@Body DataRequest3 totalMomentRequest);


    @POST("Projectile/totalPressureCD")
    Call<Result> calculateTotalPressureCD(@Body TotalPressureCDRequest request);
    @POST("Projectile/glideDistance")
    Call<Result> calculateGlideDistance(@Body GlidingDistance request);
    @POST("Projectile/glideCharacter")
    Call<Result> calculateGlidingCharacter(@Body GlidingCharacter request);

}
