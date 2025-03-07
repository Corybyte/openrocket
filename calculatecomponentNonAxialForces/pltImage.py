import matplotlib.pyplot as plt
import os


def componentNonAxialForcePLT(data, save_dir):
    if not os.path.exists(save_dir):
        os.makedirs(save_dir)
        # 绘制第一张图
    plt.figure()
    plt.plot(data)
    plt.title('figure1')
    plt.xlabel('x')
    plt.ylabel('y')
    plt.savefig(os.path.join(save_dir, 'figure1.png'))
    plt.close()
